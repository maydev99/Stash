package com.bombadu.stash

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_password_reset.*

class PasswordReset : AppCompatActivity() {

    //private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        val resetEditText = findViewById<EditText>(R.id.resetEditText)

        passwordResetButton.setOnClickListener {


            var auth = FirebaseAuth.getInstance()
            val emailText = resetEditText.text.toString()
            auth.sendPasswordResetEmail(emailText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("PASS RESET", "Email sent.")
                        emailEditText.text = null
                        Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }
}
