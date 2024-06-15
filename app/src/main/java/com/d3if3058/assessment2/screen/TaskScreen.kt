package com.d3if3058.assessment2.screen

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.d3if3058.assessment2.R
import com.d3if3058.assessment2.model.Task
import com.d3if3058.assessment2.navigation.Screen
import com.d3if3058.assessment2.ui.theme.Assessment2Theme
import com.d3if3058.assessment2.util.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.credentials.*
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.d3if3058.assessment2.BuildConfig
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import com.d3if3058.assessment2.model.User
import com.d3if3058.assessment2.network.UserDataStore
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.exceptions.ClearCredentialException
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d3if3058.assessment2.network.Api
import com.d3if3058.assessment2.network.ApiStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dataStore = SettingsDataStore(context)
    val dataStoreGoogle = UserDataStore(context)

    val user by dataStoreGoogle.userFlow.collectAsState(User())
    val viewModel: MainViewModel = viewModel()

    val errorMsg by viewModel.errorMsg.collectAsState()
    val showList by dataStore.layoutFlow.collectAsState(true)


    LaunchedEffect(user) {
        Log.d("TaskScreen", "user_id by data store: ${user.user_id}")
        Log.d("TaskScreen", "user: $user")
        if (user.user_id != -1 && user.user_id != null) {
            viewModel.getUserTasks(user.user_id!!)
        }
    }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(errorMsg) {
        Log.d("TaskScreen", "Task: $errorMsg")
    }



    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.kembali),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = {
                    Text(
                        text = if (showList) {
                            stringResource(id = R.string.list_of_todo)  // Text for list view
                        } else {
                            stringResource(id = R.string.grid_of_todo)  // Text for grid view
                        }
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStore.saveLayout(!showList)
                        }
                    }) {
                        Icon(
                            painter = painterResource(
                                if (showList) R.drawable.baseline_grid_view_24
                                else R.drawable.baseline_view_list_24
                            ),
                            contentDescription = stringResource(
                                if (showList) R.string.grid
                                else R.string.list
                            ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.About.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.tentang_aplikasi),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                signIn(
                                    context,
                                    dataStoreGoogle,
                                    viewModel
                                )
                            }
                        } else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = stringResource(R.string.profil),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (user.user_id != -1 && user.user_id != null) {
                    navController.navigate(Screen.FormBaru.withId(user.user_id!!))
                } else {
                    Toast.makeText(context, "Harap login terlebih dahulu.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.tambah_catatan),
                    tint = MaterialTheme.colorScheme.primary
                )

            }
        }
    ) { paddingValues ->
        user.user_id?.let {
            ScreenContent(
                showList, Modifier.padding(paddingValues), navController, viewModel,
                it
            )
        }
        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch {
                    signOut(
                        context,
                        dataStoreGoogle,
                        viewModel
                    )
                }
                showDialog = false
            }
        }
    }
}

@Composable
fun ScreenContent(
    showList: Boolean,
    modifier: Modifier,
    navController: NavHostController,
    viewModel: MainViewModel,
    userId: Int
) {
    val data by viewModel.tasksList.collectAsState()
    val apiStatus by viewModel.apiStatus.collectAsState()


    when (apiStatus) {
        ApiStatus.LOADING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        ApiStatus.SUCCESS -> {
            if (showList) {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(data!!) {
                        ListItem(
                            task = it, viewModel, userId
                        ) {
                            navController.navigate(Screen.FormUbah.withId(userId, it.task_id))
                        }
                        Divider()
                    }
                }

            } else {
                LazyVerticalStaggeredGrid(
                    modifier = modifier.fillMaxSize(),
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 84.dp)
                ) {
                    items(data!!) {
                        GridItem(task = it, viewModel, userId) {
                            navController.navigate(Screen.FormUbah.withId(userId, it.task_id))
                        }
                    }
                }
            }
        }


        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.error),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        }
    }


@Composable
fun ListItem(task: Task, mainViewModel: MainViewModel, userId: Int, onClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel: DetailViewModel = viewModel()
    val deleteStatus by viewModel.deleteStatus.collectAsState()


    LaunchedEffect(deleteStatus) {
        if (deleteStatus) {
            Toast.makeText(context, "Delete berhasil!", Toast.LENGTH_SHORT).show()
            viewModel.clearDeleteStatus()
            mainViewModel.getUserTasks(userId)
        }
    }
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Api.getImageUrl(task.image_id))
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.baseline_broken_image_24),
            modifier = Modifier
                .height(100.dp)
                .aspectRatio(1f)
                .padding(4.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = task.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp, top = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = task.deskripsi,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = when (task.status_prioritas) {
                    0 -> "Non-Prioritas"
                    else -> "Prioritas"
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
            )
        }
        Column {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.hapusTugas)
                )
            }
            DisplayALertDialog(
                openDialog = showDialog,
                onDismissRequest = { showDialog = false }) {
                showDialog = false
                viewModel.deleteTask(task.task_id)
            }

            IconButton(onClick = {
                shareData(
                    context = context,
                    message = context.getString(
                        R.string.bagikanTemplate,
                        task.title,
                        task.deskripsi,
                    ),
                    priority = when (task.status_prioritas) {
                        0 -> "Non-Prioritas"
                        else -> "Prioritas"
                    },
                    image = Api.getImageUrl(task.image_id)
                )
            }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(id = R.string.bagikan),
                    Modifier.scale(0.8f)
                )
            }
        }
    }
}

@Composable
fun GridItem(task: Task, mainViewModel: MainViewModel, userId: Int, onClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel: DetailViewModel = viewModel()
    val deleteStatus by viewModel.deleteStatus.collectAsState()

    LaunchedEffect(deleteStatus) {
        if (deleteStatus) {
            Toast.makeText(context, "Delete berhasil!", Toast.LENGTH_SHORT).show()
            viewModel.clearDeleteStatus()
            mainViewModel.getUserTasks(userId)
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var showDialogDelete by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
            .border(1.dp, Color.Gray),
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Api.getImageUrl(task.image_id))
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.baseline_broken_image_24),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = task.deskripsi,
                    color = Color.White
                )
                Text(
                    text = when (task.status_prioritas) {
                        0 -> "Non-Prioritas"
                        else -> "Prioritas"
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            IconButton(onClick = {
                showDialogDelete = true;
            }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                if (showDialogDelete) {
                    DeleteDialog(
                        onDismissRequest = { showDialogDelete = false }) {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.deleteTask(task.task_id)
                        }
                        showDialogDelete = false
                    }
                }
            }
        }
    }
}


@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Anda yakin ingin menghapus data ini?",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Tutup")
                    }
                    OutlinedButton(
                        onClick = { onConfirmation() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Hapus")
                    }
                }
            }
        }
    }
}

private fun shareData(context: Context, message: String, priority: String, image: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        var messageWithPriority = message
        if (priority.isNotBlank()) {
            messageWithPriority += "\nStatus: $priority"
        }
        if (image.isNotBlank()) {
            messageWithPriority += "\nImage: $image"
        }
        putExtra(Intent.EXTRA_TEXT, messageWithPriority)
    }
    if (shareIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(shareIntent)
    }
}


private suspend fun signIn(context: Context, dataStore: UserDataStore, viewModel: MainViewModel) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()


    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore, viewModel)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore,
    viewModel: MainViewModel
) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        try {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleIdToken.displayName ?: ""
            val email = googleIdToken.id
            Log.d("TaskScreen", "Email: $email")
            val photoUrl = googleIdToken.profilePictureUri.toString()
            dataStore.saveData(User(-1, nama, email, photoUrl))
            viewModel.identifyUser(email, dataStore)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore, viewModel: MainViewModel) {
    try {
        viewModel.logout()
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun GreetingPreview() {
    Assessment2Theme {
        MainScreen(rememberNavController())
    }
}