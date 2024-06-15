package com.d3if3058.assessment2.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d3if3058.assessment2.model.Task
import com.d3if3058.assessment2.model.UserCreate
import com.d3if3058.assessment2.network.Api
import com.d3if3058.assessment2.network.ApiStatus
import com.d3if3058.assessment2.network.UserDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel() : ViewModel() {
//    val optionArray : Array<String> = arrayOf("Prioritas", "Non-Prioritas")

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId

    private val _tasksList = MutableStateFlow<List<Task>?>(emptyList())
    val tasksList: StateFlow<List<Task>?> = _tasksList

    private val _deleteStatus = MutableStateFlow(false)
    val deleteStatus: StateFlow<Boolean> = _deleteStatus

    private val _apiStatus = MutableStateFlow(ApiStatus.LOADING)
    val apiStatus: StateFlow<ApiStatus> = _apiStatus

    fun identifyUser(userEmail: String, dataStore: UserDataStore) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _userId.value = Api.service.identifyUser(UserCreate(userEmail))
                dataStore.setUserId(_userId.value!!)
//                getUserTasks(_userId.value!!)
                Log.d("MainVM", "userId: ${_userId.value}")
            } catch (e: HttpException) {
                _errorMsg.value =
                    e.response()?.errorBody()?.string()?.replace(Regex("""[{}":]+"""), "")
                        ?.replace("detail", "")
            }
        }
    }

    fun getUserTasks(userId: Int) {
        _apiStatus.value = ApiStatus.LOADING
        Log.d("MainVM", "getUser ID: $userId")
            Log.d("MainVM", "User Tasks before query: ${_tasksList.value}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (userId != -1) {
                    _tasksList.value = Api.service.getUserTasks(userId)
                    _apiStatus.value = ApiStatus.SUCCESS
            Log.d("MainVM", "User Tasks: ${_tasksList.value}")
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
                _apiStatus.value = ApiStatus.FAILED
            }
        }
    }


    fun logout() {
        _tasksList.value = emptyList()
        _userId.value = null
    }

    fun clearApiStatus() {
        _apiStatus.value = ApiStatus.LOADING
    }
}