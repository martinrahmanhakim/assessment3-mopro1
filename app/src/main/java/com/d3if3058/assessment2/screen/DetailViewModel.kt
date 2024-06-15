package com.d3if3058.assessment2.screen

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d3if3058.assessment2.model.Task
import com.d3if3058.assessment2.model.TaskCreate
import com.d3if3058.assessment2.model.TaskUpdate
import com.d3if3058.assessment2.network.Api
import com.d3if3058.assessment2.network.ApiStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream


class DetailViewModel() : ViewModel() {
//    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)


    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    private val _taskDetail = MutableStateFlow<Task?>(null)
    val taskDetail: StateFlow<Task?> = _taskDetail

    private val _uploadStatus = MutableStateFlow(false)
    val uploadStatus: StateFlow<Boolean> = _uploadStatus

    private val _deleteStatus = MutableStateFlow(false)
    val deleteStatus: StateFlow<Boolean> = _deleteStatus

    private val _updateStatus = MutableStateFlow(false)
    val updateStatus: StateFlow<Boolean> = _updateStatus




    fun getTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _taskDetail.value = Api.service.getTaskDetail(taskId)
                Log.d("DetailVM", "Task Detail: ${_taskDetail.value}")
            } catch (e: HttpException) {
                _errorMsg.value = e.response()?.errorBody()?.string()?.replace(Regex("""[{}":]+"""), "")
                    ?.replace("detail", "")
            }
        }
    }

    fun addTask(task: TaskCreate, user_id: Int, fileUri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(fileUri)
                val fileExtension = getFileExtension(fileUri, contentResolver)
                val file = File.createTempFile("upload", ".$fileExtension", null)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestBody)
                Log.d("DetailVM", "User ID: $user_id")
                _uploadStatus.value = Api.service.addTask(task.title, task.deskripsi, task.status_prioritas, user_id, body).status
            } catch (e: HttpException) {
                _errorMsg.value = e.response()?.errorBody()?.string()?.replace(Regex("""[{}":]+"""), "")
                    ?.replace("detail", "")
//                Log.d("DetailVM", "Error Upload: ${_errorMsg.value}")
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _deleteStatus.value = Api.service.deleteTask(taskId).status
            } catch (e: HttpException) {
                _errorMsg.value = e.response()?.errorBody()?.string()?.replace(Regex("""[{}":]+"""), "")
                    ?.replace("detail", "")
            }
        }
    }

    fun updateTask(task: TaskUpdate, fileUri: Uri? = null, contentResolver: ContentResolver) {
        Log.d("DetailVM", "FileUri: $fileUri")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = fileUri?.let { contentResolver.openInputStream(it) }
                val fileExtension = fileUri?.let { getFileExtension(it, contentResolver) }
                val file = File.createTempFile("upload", ".$fileExtension", null)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestBody)
                if (fileUri == null) {
                    _updateStatus.value = Api.service.updateTaskWithoutImg(task.task_id, task.title, task.deskripsi, task.status_prioritas).status
                } else {
                    _updateStatus.value = Api.service.updateTask(task.task_id, task.title, task.deskripsi, task.status_prioritas, body).status
                }
            } catch (e: HttpException) {
                _errorMsg.value = e.response()?.errorBody()?.string()?.replace(Regex("""[{}":]+"""), "")
                    ?.replace("detail", "")
            }
        }
    }

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        val mimeType = contentResolver.getType(uri)
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> null
        }
    }

    fun clearErrorMsg() {
        _errorMsg.value = null
    }
    fun clearUploadStatus() {
        _uploadStatus.value = false
    }
    fun clearUpdateStatus() {
        _updateStatus.value = false
    }

    fun clearDeleteStatus() {
        _deleteStatus.value = false
    }



//    fun update(id: Long, judul: String, isi: String, prioritas: String) {
//        val task = Task(
//            id = id,
//            judul = judul,
//            isi = isi,
//            prioritas = prioritas
//
//        )
//
//        viewModelScope.launch(Dispatchers.IO) {
//            dao.update(task)
//        }
//    }
//
//    fun delete(id: Long) {
//        viewModelScope.launch(Dispatchers.IO) {
//            dao.deleteById(id)
//        }
//    }
}


