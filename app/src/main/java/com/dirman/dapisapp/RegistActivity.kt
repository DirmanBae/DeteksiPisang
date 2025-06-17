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
import com.google.firebase.database.FirebaseDatabase

class RegistActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_regist)
        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPass)

        val btnRegis = findViewById<Button>(R.id.btnRegist)
        val btnLogin = findViewById<TextView>(R.id.btnLogin)

        btnLogin.setOnClickListener(){
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }

        btnRegis.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPass.text.toString()
            val name = etName.text.toString()
            val level = "Petani"

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid

                            val user = hashMapOf(
                                "uid" to uid,
                                "email" to email,
                                "nama" to name,
                                "level" to level
                            )

                            if (uid != null) {
                                FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registrasi dan penyimpanan data sukses!", Toast.LENGTH_SHORT).show()
                                        // Setelah sukses, boleh pindah activity
                                        val i = Intent(this, LoginActivity::class.java)
                                        startActivity(i)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Gagal menyimpan data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Registrasi Gagal: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Silakan lengkapi form!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}