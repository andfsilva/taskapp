package com.example.taskapp

data class Task(
    val id: Int,
    val description: String,
    val deadline_date: String?,
    val deadline_time: String?,
    var notify: Boolean
)
