package com.example.taskapp

import android.content.Context
import android.graphics.Color
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.taskapp.databinding.ItemTaskBinding
import com.example.taskapp.databinding.ActivityAddTaskBinding
import java.text.SimpleDateFormat
import java.util.*
import java.text.ParseException

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers

class TaskAdapter(private val listener: OnItemClickListener, private val context: Context) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private var tasks: List<Task> = ArrayList()

    interface OnItemClickListener {
        fun onDeleteClick(task: Task)
        fun onEditClick(task: Task)
    }

    fun submitList(taskList: List<Task>) {
        tasks = taskList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.textViewDescription.text = task.description
            binding.textViewDeadline.text = "${task.deadline_date} ${task.deadline_time}"


            // Verificar se a data da tarefa é anterior à data atual
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val deadlineString = "${task.deadline_date} ${task.deadline_time}"
            val deadlineDate = try {
                sdf.parse(deadlineString)
            } catch (e: ParseException) {
                null
            }
            val now = Calendar.getInstance().time

            if (deadlineDate != null && deadlineDate.before(now)) {
                // Tarefa atrasada, mostrar o texto "Atrasado" e alterar a cor de fundo
                binding.textViewStatus.visibility = View.VISIBLE
                binding.root.setBackgroundColor(Color.parseColor("#F6C5AC"))
            } else {
                // Tarefa não atrasada, esconder o texto "Atrasado" e cor normal
                binding.textViewStatus.visibility = View.GONE
                binding.root.setBackgroundColor(Color.WHITE)
            }

            // Resete o listener antes de definir o estado da checkbox
            binding.checkboxTask.setOnCheckedChangeListener(null)
            binding.checkboxTask.isChecked = false  // Defina a checkbox como desmarcada inicialmente



            // Adicione o listener da checkbox
            binding.checkboxTask.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val db = DatabaseHelper(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        if (db.updateTaskNotify(task.id, isChecked)) {
                            // Use a dispatcher to return to the UI thread
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Tarefa finalizada", Toast.LENGTH_SHORT).show()
                                listener.onDeleteClick(task)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Erro ao atualizar notificação", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            // Configure a checkbox de notificação
            binding.checkboxNotify.setOnCheckedChangeListener(null)
            binding.checkboxNotify.isChecked = task.notify
            binding.checkboxNotify.setOnCheckedChangeListener { _, isChecked ->
                val db = DatabaseHelper(context)
                db.updateTaskNotify(task.id, isChecked)
                task.notify = isChecked
            }

            binding.buttonDelete.setOnClickListener {
                listener.onDeleteClick(task)
            }

            binding.buttonEdit.setOnClickListener {
                listener.onEditClick(task)
            }
        }
    }

}



