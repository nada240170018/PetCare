package com.example.petcare

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackLogin = findViewById<Button>(R.id.btnBackLogin)

        btnRegister.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(
                    this,
                    "Semua kolom wajib diisi",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                !email.contains("@") ||
                !email.substringAfter("@").contains(".")
            ) {
                Toast.makeText(
                    this,
                    "Format email tidak valid",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(
                    this,
                    "Password minimal 6 karakter",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(
                    this,
                    "Konfirmasi password tidak cocok",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    btnRegister.isEnabled = true

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Pendaftaran berhasil!",
                            Toast.LENGTH_LONG
                        ).show()

                        auth.signOut()

                        startActivity(
                            Intent(this, LoginActivity::class.java)
                        )
                        finish()

                    } else {

                        val message = when (task.exception) {

                            is FirebaseAuthUserCollisionException ->
                                "Email sudah terdaftar. Gunakan email lain."

                            is FirebaseAuthWeakPasswordException ->
                                "Password terlalu lemah. Gunakan minimal 6 karakter."

                            is FirebaseNetworkException ->
                                "Koneksi internet bermasalah. Periksa jaringanmu."

                            else -> {
                                if (task.exception?.message?.contains(
                                        "too-many-requests",
                                        ignoreCase = true
                                    ) == true
                                ) {
                                    "Terlalu banyak percobaan. Coba lagi nanti."
                                } else {
                                    "Pendaftaran gagal. Silakan coba kembali."
                                }
                            }
                        }

                        Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        btnBackLogin.setOnClickListener {
            finish()
        }
    }
}
