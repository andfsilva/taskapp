package com.example.taskapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskapp.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("TaskApp", MODE_PRIVATE)

        loadSettings()

        binding.btnSaveSettings.setOnClickListener {
            val newUserName = binding.editUserName.text.toString()

            saveSettings()


            if (newUserName.isNotBlank()) {
                sharedPreferences.edit().putString("userName", newUserName).apply()

                Toast.makeText(this, "Nome de usuário atualizado!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nome de usuário inválido!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            // Criar um Intent para voltar para a Activity principal
            val intent = Intent(this, MainActivity::class.java)
            // (opcional) Se você precisar passar dados de volta
            //intent.putExtra("data", "algum dado")
            startActivity(intent)
            finish() // Importante! Fecha a Activity atual
        }
    }

    private fun loadSettings() {
        val userName = sharedPreferences.getString("userName", "Usuário não definido")
        //binding.userNameTextView.text = userName
        binding.editUserName.setText(userName)

        val notificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)
        //Anderson
//        binding.switchNotifications.isChecked = notificationsEnabled


    }



    private fun saveSettings() {
        val newUserName = binding.editUserName.text.toString()
        if (newUserName.isNotBlank()) {
            sharedPreferences.edit().putString("userName", newUserName).apply()


            loadSettings()  // Update the UI with the new username
        } else {
            Toast.makeText(this, "Nome de usuário inválido!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveNotifications(isChecked: Boolean) {
        sharedPreferences.edit().putBoolean("notificationsEnabled", isChecked).apply()


    }
}
