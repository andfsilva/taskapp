package com.example.taskapp

import android.content.Intent
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskapp.databinding.ActivityAddTaskBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var taskIdForEdit: Int = -1
    //MANTENDO O MESMO FORMATO DE DATA E HORA NO APLICATIVO INTEIRO
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val utcDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnPickTime.setOnClickListener { showTimePicker() }
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        taskIdForEdit = intent.getIntExtra("task_id", -1)
        if (taskIdForEdit != -1) {
            loadTaskData(taskIdForEdit)
            binding.btnSaveTask.setOnClickListener {
                updateTask(taskIdForEdit)
            }
        } else {
            binding.btnSaveTask.setOnClickListener {
                saveTask()
            }
        }
    }

    private fun loadTaskData(taskId: Int) {
        val db = DatabaseHelper(this)
        val task = db.getAllTasks().find { it.id == taskId }

        task?.let {
            binding.editTaskDescription.setText(it.description)
            selectedDate = it.deadline_date
            selectedTime = it.deadline_time

            binding.textDate.text = selectedDate ?: "Sem data definida"
            binding.textTime.text = selectedTime ?: "Sem hora definida"
            binding.switchNotify.isChecked = it.notify
        }
    }


    private fun updateTask(taskId: Int) {
        val description = binding.editTaskDescription.text.toString()
        val notify = binding.switchNotify.isChecked

        if (description.isBlank()) {
            Toast.makeText(this, "A descrição não pode estar vazia!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseHelper(this)
        val success = db.updateTask(taskId, description, selectedDate, selectedTime, notify)

        if (success) {
            Toast.makeText(this, "Tarefa atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Erro ao atualizar tarefa.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                selectedDate = dateFormat.format(calendar.time)
                binding.textDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                selectedTime = timeFormat.format(calendar.time)
                binding.textTime.text = selectedTime
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun saveTask() {
        val description = binding.editTaskDescription.text.toString()
        val notify = binding.switchNotify.isChecked

        if (description.isBlank()) {
            Toast.makeText(this, "A descrição não pode estar vazia!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseHelper(this)
        db.addTask(description, selectedDate, selectedTime, notify)

        Toast.makeText(this, "Tarefa salva com sucesso!", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

}