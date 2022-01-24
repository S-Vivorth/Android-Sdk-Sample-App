package com.example.androidsdksampleapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_homescreen.*

class homescreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_homescreen)
        toCheckoutBtn.setOnClickListener {
            startActivity(Intent(applicationContext,
                checkOutJava::class.java))
            finish()
        }
    }
}