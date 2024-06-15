package com.d3if3058.assessment2.network

import com.d3if3058.assessment2.model.MessageResponse
import com.d3if3058.assessment2.model.Task
import com.d3if3058.assessment2.model.User
import com.d3if3058.assessment2.model.UserCreate
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://04ae-139-228-112-175.ngrok-free.app"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ApiService{

    @POST("/v1/users/")
    suspend fun identifyUser(
        @Body user_email: UserCreate
    ): Int

    @Multipart
    @POST("/v1/tasks/")
    suspend fun addTask(
        @Query("title") title: String,
        @Query("deskripsi") deskripsi: String,
        @Query("status_prioritas") status: Int,
        @Query("user_id") user_id: Int,
        @Part image: MultipartBody.Part
    ): MessageResponse

    @GET("/v1/tasks/{task_id}")
    suspend fun getTaskDetail(
        @Path("task_id") task_id: Int
    ): Task
    @GET("/v1/tasks/")
    suspend fun getUserTasks(
        @Query("user_id") task_id: Int
    ): List<Task>

    @DELETE("/v1/tasks/{task_id}")
    suspend fun deleteTask(
        @Path("task_id") id: Int
    ): MessageResponse

    @Multipart
    @PUT("/v1/tasks/{task_id}")
    suspend fun updateTask(
        @Path("task_id") id: Int,
        @Query("title") title: String? = null,
        @Query("deskripsi") deskripsi: String? = null,
        @Query("status_prioritas") status: Int? = null,
        @Part image: MultipartBody.Part
    ): MessageResponse

    @PUT("/v1/tasks/{task_id}")
    suspend fun updateTaskWithoutImg(
        @Path("task_id") id: Int,
        @Query("title") title: String? = null,
        @Query("deskripsi") deskripsi: String? = null,
        @Query("status_prioritas") status: Int? = null,
    ): MessageResponse

}

object Api{
    val service: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun getImageUrl(imageId: String): String{
        return "${BASE_URL}/v1/tasks/image/$imageId"
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }