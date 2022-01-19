package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityHomescreenBinding
import com.example.myapplication.databinding.ActivityMainBinding

class homescreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomescreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toCheckoutBtn.setOnClickListener {
            startActivity(Intent(applicationContext,MainActivity::class.java))
        }
    }
}