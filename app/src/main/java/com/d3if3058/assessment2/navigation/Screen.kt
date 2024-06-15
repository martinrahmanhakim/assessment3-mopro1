package com.d3if3058.assessment2.navigation


const val KEY_ID_TASK = "idTask"
const val KEY_ID_USER = "idUser"

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcomeScreen")
    data object Home : Screen("mainScreen")
    data object About : Screen("aboutScreen")
    data object FormBaru : Screen("detailScreen/{$KEY_ID_USER}") {
        fun withId(id: Int) = "detailScreen/$id"
    }

    data object FormUbah : Screen("detailScreen/{$KEY_ID_USER}/{$KEY_ID_TASK}") {
        fun withId(userId: Int, taskId: Int) = "detailScreen/$userId/$taskId"
    }
}