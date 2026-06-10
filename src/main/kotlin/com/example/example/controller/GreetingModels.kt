package com.example.example.controller

import java.util.*

data class GreetingMain(
    val text: String
)

data class GreetingUser(
    val text: String,
    val id: UUID
)

data class UserData(
    val name: String,
    val surname: String
)