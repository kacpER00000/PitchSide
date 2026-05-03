package com.example.pitchside.managers
import com.example.pitchside.data.User
object SessionManager {
    var loggedInUser: User? = null

    fun isLoggedIn(): Boolean = loggedInUser != null
}