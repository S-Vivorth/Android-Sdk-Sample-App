package com.bill24.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.R
import kotlinx.android.synthetic.main.activity_homescreen.*

class homescreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_homescreen)
        toCheckoutBtn.setOnClickListener {
            startActivity(Intent(applicationContext, Main::class.java))
            finish()
        }
    }
}