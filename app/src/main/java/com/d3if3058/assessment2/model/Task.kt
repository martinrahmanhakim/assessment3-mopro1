package com.d3if3058.assessment2.model


data class Task(
    val task_id: Int,
    val title: String,
    val deskripsi: String,
    val status_prioritas: Int,
    val image_id: String
)

data class TaskCreate(
    val title: String,
    val deskripsi: String,
    val status_prioritas: Int
)

data class TaskUpdate(
    val task_id: Int,
    val title: String? = null,
    val deskripsi: String? = null,
    val status_prioritas: Int? = null
)