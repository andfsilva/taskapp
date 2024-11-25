package com.example.taskapp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TaskNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val taskId = inputData.getInt("task_id", -1)

        if (taskId == -1) {
            Log.e("TaskNotificationWorker", "ID da tarefa inválido")
            return Result.failure()
        }

        val db = DatabaseHelper(applicationContext)
        val task = db.getAllTasks().find { it.id == taskId }

        if (task != null) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showNotification(
                "Tarefa Pendente: ${task.description}",
                "Prazo: ${task.deadline_date} ${task.deadline_time}"
            )
        } else {
            Log.e("TaskNotificationWorker", "Tarefa com ID $taskId não encontrado")
        }
        return Result.success()
    }
}
