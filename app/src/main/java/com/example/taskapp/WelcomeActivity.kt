package com.example.taskapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskapp.databinding.ActivityWelcomeBinding // Import your binding
import com.example.taskapp.MainActivity


class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("TaskApp", MODE_PRIVATE)

        val hasEnteredName = sharedPreferences.getBoolean("hasEnteredName", false)
        if(hasEnteredName){
            //If the user entered their name, redirect to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnContinue.setOnClickListener {
            val username = binding.editName.text.toString()
            if (username.isNotBlank()) {
                sharedPreferences.edit().putString("userName", username).apply()
                sharedPreferences.edit().putBoolean("hasEnteredName", true).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Nome de usuário inválido!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}