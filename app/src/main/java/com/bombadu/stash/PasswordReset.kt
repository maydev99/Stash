package com.bombadu.stash

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_password_reset.*


class PasswordReset : AppCompatActivity() {

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        val resetEditText = findViewById<EditText>(R.id.resetEditText)

        passwordResetButton.setOnClickListener {


            val auth = FirebaseAuth.getInstance()
            val emailText = resetEditText.text.toString()
            auth.sendPasswordResetEmail(emailText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("PASS RESET", "email sent.")
                        finish()
                        Toast.makeText(this, "email Sent", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(this, "email reset failed, check email address", Toast.LENGTH_SHORT).show()


                }

        }
    }

}
