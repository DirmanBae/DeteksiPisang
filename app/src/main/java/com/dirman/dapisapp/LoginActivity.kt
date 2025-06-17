package com.dirman.dapisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

//        auth
        auth = FirebaseAuth.getInstance()
        val etEmail = findViewById<EditText>(R.id.etEmailLogin)
        val etPass = findViewById<EditText>(R.id.etPassLogin)

        val btnDaftar = findViewById<TextView>(R.id.btnDaftar)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener(){
            val email = etEmail.text.toString()
            val password = etPass.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login sukses!", Toast.LENGTH_SHORT).show()
                            // Setelah sukses, pindah ke MainActivity
                            val i = Intent(this, HomeActivity::class.java)
                            startActivity(i)
                            finish()
                        } else {
                            Toast.makeText(this, "Login Gagal: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Silakan isi email dan password!", Toast.LENGTH_SHORT).show()
            }
        }

        btnDaftar.setOnClickListener(){
            val i = Intent(this, RegistActivity::class.java)
            startActivity(i)
        }
    }
}