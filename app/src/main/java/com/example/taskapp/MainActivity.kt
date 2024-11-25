package com.example.taskapp



import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.taskapp.databinding.ActivityMainBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), TaskAdapter.OnItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private val taskAdapter = TaskAdapter(this, this)
    private lateinit var sharedPreferences: SharedPreferences
    private val ADD_TASK_REQUEST_CODE = 1
    private val EDIT_TASK_REQUEST_CODE = 2
    private val PERMISSION_REQUEST_CODE = 100
    private val utcDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
        scheduleNotifications()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("TaskApp", MODE_PRIVATE)












        val userName = sharedPreferences.getString("userName", null)
        Log.d("MainActivity", "Username from SharedPreferences: $userName")

        if (userName == null) {
            Log.d("MainActivity", "Username not found, starting WelcomeActivity")
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        else {
            Log.d("MainActivity", "Username found: $userName")

        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter

        binding.btnAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivityForResult(intent, ADD_TASK_REQUEST_CODE)
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadTasks()
        scheduleNotifications()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            loadTasks()
            scheduleNotifications()
        } else if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            loadTasks()
            scheduleNotifications()
        }
    }

    override fun onDeleteClick(task: Task) {
        val db = DatabaseHelper(this)
        db.deleteTask(task.id)
        loadTasks()
        scheduleNotifications()
        Toast.makeText(this, "Tarefa excluída com sucesso!", Toast.LENGTH_SHORT).show()
    }

    override fun onEditClick(task: Task) {
        val intent = Intent(this, AddTaskActivity::class.java)
        intent.putExtra("task_id", task.id)
        intent.putExtra("description", task.description)
        intent.putExtra("deadline_date", task.deadline_date)
        intent.putExtra("deadline_time", task.deadline_time)
        intent.putExtra("notify", task.notify)
        startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
    }

    private fun loadTasks() {
        val db = DatabaseHelper(this)
        val tasks = db.getAllTasks()

        val userName = sharedPreferences.getString("userName", "Usuário")
        binding.userName.text = userName

        // Ordenar tarefas por data em ordem decrescente
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis

        val sortedTasks = if (tasks.isEmpty()) {
            emptyList()
        } else {
            tasks.sortedWith(compareBy<Task> { task ->
                try {
                    val deadlineString = "${task.deadline_date} ${task.deadline_time}"
                    val deadlineDate = utcDateFormat.parse(deadlineString)
                    if (deadlineDate == null) {
                        Long.MAX_VALUE
                    } else if (deadlineDate.time < now) {
                        Long.MAX_VALUE
                    } else {
                        deadlineDate.time
                    }
                } catch (e: ParseException) {
                    Long.MAX_VALUE
                }
            })
        }
        taskAdapter.submitList(sortedTasks)
    }

    private fun scheduleNotificationForTask(task: Task) {
        try {
            val deadlineString = "${task.deadline_date} ${task.deadline_time}"
            val deadlineUTC = utcDateFormat.parse(deadlineString) ?: return

            val nowUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
            val delayMillis = deadlineUTC.time - nowUTC

            if (delayMillis > 0) {
                val workRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .addTag("task_${task.id}")
                    .setInputData(workDataOf("task_id" to task.id))
                    .build()

                WorkManager.getInstance(this).enqueueUniqueWork(
                    "task_${task.id}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
                Log.d("MainActivity", "Agendar notificação para tarefa ${task.id} em ${delayMillis} milissegundos")
            } else {
                Log.d("MainActivity", "Tarefa ${task.id} atrasada")
            }

        } catch (e: ParseException) {
            Log.e("MainActivity", "Error parsing date: ${e.message}", e)
        } catch (e: NullPointerException) {
            Log.e("MainActivity", "Null date/time value for task ${task.id}")
        }
    }

    private fun scheduleNotifications() {
        WorkManager.getInstance(this).cancelAllWorkByTag("task_*")

        val db = DatabaseHelper(this)
        val tasks = db.getAllTasks()

        for (task in tasks) {
            if (task.notify) {
                scheduleNotificationForTask(task)
            }
        }
    }
}

