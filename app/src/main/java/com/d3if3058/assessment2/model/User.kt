package com.d3if3058.assessment2.model

data class User(
    val user_id: Int? = -1,
    val name: String = "",
    val email: String = "",
    val photoUrl: String = ""
)

data class UserCreate(
    val user_email: String
)