package com.dirman.dapisapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_regist)

        val btnLogin = findViewById<TextView>(R.id.btnLogin)

        btnLogin.setOnClickListener(){
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }
    }
}