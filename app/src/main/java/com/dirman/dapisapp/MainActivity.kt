package com.dirman.dapisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val btnMulai = findViewById<Button>(R.id.btnStart)
        btnMulai.setOnClickListener{
         val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
    }
    }
}