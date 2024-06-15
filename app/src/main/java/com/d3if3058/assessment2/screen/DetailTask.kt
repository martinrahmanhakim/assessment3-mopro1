package com.d3if3058.assessment2.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d3if3058.assessment2.R
import com.d3if3058.assessment2.model.TaskCreate
import com.d3if3058.assessment2.model.TaskUpdate
import com.d3if3058.assessment2.network.Api
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavHostController, userId: Int, taskId: Int? = null) {
    val context = LocalContext.current
    val viewModel: DetailViewModel = viewModel()
    val contentResolver = context.contentResolver

    val taskData by viewModel.taskDetail.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val deleteStatus by viewModel.deleteStatus.collectAsState()

    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.getTask(taskId)
        }
    }

    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            Log.d("DetailTask", "errorMsg: $errorMsg")
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMsg()
        }
    }

    LaunchedEffect(uploadStatus) {
        if (uploadStatus) {
            Toast.makeText(context, "Upload berhasil!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.clearUploadStatus()
        }
    }

    LaunchedEffect(updateStatus) {
        if (updateStatus) {
            Toast.makeText(context, "Update berhasil!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.clearUpdateStatus()
        }
    }

    LaunchedEffect(deleteStatus) {
        if (deleteStatus) {
            Toast.makeText(context, "Delete berhasil!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.clearDeleteStatus()
        }
    }

    var judul by remember { mutableStateOf("") }
    var isi by remember { mutableStateOf("") }
    var prioritas by remember { mutableStateOf("") }
    var imageId by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }


    LaunchedEffect(taskData) {
        if (taskData != null) {
            judul = taskData!!.title
            isi = taskData!!.deskripsi
            prioritas = when (taskData!!.status_prioritas) {
                0 -> "Non-Prioritas"
                else -> "Prioritas"
            }
            imageId = taskData!!.image_id
            Log.d("DetailTask", "ImageID: $imageId")
        }
    }

    LaunchedEffect(photoUri) {
        if (photoUri != null) {
            imageId = null
        }
    }
    var showDialog by remember { mutableStateOf(false) }
    var judulError by rememberSaveable {
        mutableStateOf(false)
    }
    var isiError by rememberSaveable {
        mutableStateOf(false)
    }

    val radioOption = listOf("Prioritas", "Non-Prioritas")
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(
                                id = R.string.kembali
                            ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = {
                    if (taskId == null)
                        Text(
                            text = stringResource(id = R.string.tambah_catatan)
                        )
                    else
                        Text(
                            text = stringResource(id = R.string.edit_catatan)
                        )

                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    Log.d("Detail Task", "User ID Before Upload: $userId")
                    photoUri?.let {
                        IconButton(onClick = {
                            if (judul == "" || isi == "" || prioritas == "") {
                                Toast.makeText(context, R.string.invalid, Toast.LENGTH_SHORT)
                                    .show()
                                return@IconButton
                            }
//
                            if (taskId == null) {
                                viewModel.addTask(
                                    TaskCreate(
                                        judul, isi, when (prioritas) {
                                            "Non-Prioritas" -> 0
                                            else -> 1
                                        }
                                    ),
                                    userId,
                                    it,
                                    contentResolver

                                )
                            } else {
                                    viewModel.updateTask(
                                        TaskUpdate(
                                            taskId, judul, isi, when (prioritas) {
                                                "Non-Prioritas" -> 0
                                                else -> 1
                                            }
                                        ), it,
                                        contentResolver
                                    )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = stringResource(
                                    id = R.string.simpan
                                ),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (taskId != null && photoUri == null) {
                        IconButton(onClick = {
                            viewModel.updateTask(
                                TaskUpdate(
                                    taskId, judul, isi, when (prioritas) {
                                        "Non-Prioritas" -> 0
                                        else -> 1
                                    }
                                ), photoUri,
                                contentResolver
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = stringResource(
                                    id = R.string.simpan
                                ),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (userId != null && taskId != null) {
                        DeleteAction {
                            showDialog = true
                        }
                        DisplayALertDialog(
                            openDialog = showDialog,
                            onDismissRequest = { showDialog = false }) {
                            showDialog = false
                            viewModel.deleteTask(taskId!!)
                            navController.popBackStack()
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        FormCatatan(
            title = judul,
            onTitleChange = { judul = it },
            desc = isi,
            onDescChange = { isi = it },
            jenis = prioritas,
            onPriorityChanged = { prioritas = it },
            modifier = Modifier.padding(paddingValues),
            radioOption = radioOption,
            photoUri = photoUri,
            image_id = imageId,
            onUriChange = { photoUri = it },
            context = context
        )
    }
}

@Composable
fun FormCatatan(
    title: String, onTitleChange: (String) -> Unit,
    desc: String, onDescChange: (String) -> Unit,
    jenis: String, onPriorityChanged: (String) -> Unit,
    radioOption: List<String>,
    modifier: Modifier,
    photoUri: Uri?,
    image_id: String? = null,
    onUriChange: (Uri) -> Unit,
    context: Context
) {
    val context2 = LocalContext.current
    val radioOptions = listOf(
        stringResource(id = R.string.prioritas),
        stringResource(id = R.string.non_prioritas)
    )

    val filePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                onUriChange(uri)
            }
        }


    var judulError by rememberSaveable {
        mutableStateOf(false)
    }
    var isiError by rememberSaveable {
        mutableStateOf(false)
    }
    var prioritasError by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { onTitleChange(it) },
            label = { Text(text = stringResource(id = R.string.judul)) },
            isError = judulError,
            trailingIcon = { IconPicker(judulError, "") },
            supportingText = { Errorhint(judulError) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = desc,
            onValueChange = { onDescChange(it) },
            label = { Text(text = stringResource(id = R.string.isi_catatan)) },
            isError = isiError,
            trailingIcon = { IconPicker(isiError, "") },
            supportingText = { Errorhint(isiError) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .padding(top = 6.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        ) {

            radioOption.forEach { text ->
                KelasOpsi(
                    label = text,
                    isSelected = jenis == text,
                    modifier = Modifier
                        .selectable(
                            selected = jenis == text,
                            onClick = { onPriorityChanged(text) },
                            role = Role.RadioButton
                        )

                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Button(onClick = {
            filePickerLauncher.launch("image/*")
        }) {
            Text(text = "Pick Image")
        }
        photoUri?.let {
            val inputStream: InputStream? = context2.contentResolver.openInputStream(it)
            val bitmap = inputStream?.let { stream -> android.graphics.BitmapFactory.decodeStream(stream) }
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )
            }
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )
            }
        }
        Log.d("DetailTask", "ImageID close to async: $image_id")
        if (image_id != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Api.getImageUrl(image_id))
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
        }

    }
}

@Composable
fun KelasOpsi(label: String, isSelected: Boolean, modifier: Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically

    ) {
        if (isSelected) {
            RadioButton(selected = true, onClick = null)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            RadioButton(selected = false, onClick = null)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun DeleteAction(delete: () -> Unit) {
    var expanded by remember {
        mutableStateOf(false)
    }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.lainya),
            tint = MaterialTheme.colorScheme.primary
        )
        DropdownMenu(expanded = expanded,
            onDismissRequest = { expanded = false }) {

            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.hapus)) },
                onClick = {
                    expanded = false
                    delete()
                })
        }

    }
}

@Composable
fun IconPicker(isError: Boolean, unit: String) {
    if (isError) {
        Icon(imageVector = Icons.Filled.Warning, contentDescription = null)
    } else {
        Text(text = unit)
    }
}

@Composable
fun Errorhint(isError: Boolean) {
    if (isError) {
        Text(text = stringResource(R.string.input_invalid))
    }
}



