package com.bombadu.stash

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*

class Auth : AppCompatActivity() {

    private var isCreatingNewAccount = false
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        auth = FirebaseAuth.getInstance()

        //For Using Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        //Email Password Sign In Button
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateForm(email, password)) {
                return@setOnClickListener
            }

            if (isCreatingNewAccount) {
                createNewAccount(email, password)
            } else {
                signIn(email, password)

            }

        }

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        //GoogleSign In Button
        gSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)

        }


        //Switches email Sign In to email Create Account
        createAccountTextView.setOnClickListener {
            if (!isCreatingNewAccount) {
                //titleTextView.text = getString(R.string.new_account)
                signInButton.text = getString(R.string.create_account)
                createAccountTextView.text = getString(R.string.tap_here_to_sign_in)
                isCreatingNewAccount = true
            } else {
                //titleTextView.text = getString(R.string.stash)
                signInButton.text = getString(R.string.sign_in)
                createAccountTextView.text = getString(R.string.tap_here_to_create_an_account)
                isCreatingNewAccount = false
            }


        }


    }
    //Checks email edit text and password edit text for empty fields
    private fun validateForm(email: String, password: String): Boolean {

        var valid = true

        if (TextUtils.isEmpty(email)) {
            emailEditText.error = getString(R.string.required)
            valid = false
        } else {
            emailEditText.error = null
        }

        if (TextUtils.isEmpty(password)){
            passwordEditText.error = getString(R.string.required)
            valid = false
        } else {
            passwordEditText.error = null
        }

        return  !valid

    }



    //Handles the process of email sign in
    private fun signIn(email: String, password: String) {
        auth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    sendEmailVerification()
                    startActivity(Intent(this, StashList::class.java))
                    makeAToast("Sign-In Successful")
                    finish()
                } else {
                    makeAToast("Sign-In Failed")
                }
            }

    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                // [START_EXCLUDE]
                // Re-enable button
                //verifyEmailButton.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(baseContext,
                        "Verification email sent to ${user.email} ",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(baseContext,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
                // [END_EXCLUDE]
            }
    }

    //Handles the process of email create account
    private fun createNewAccount(email: String, password: String) {
        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //startActivity(Intent(this, StashList::class.java))
                    sendEmailVerification()
                    makeAToast("Account Created")
                    finish()
                } else {
                    makeAToast("Account Creation Failed")
                }
            }

    }

    private fun makeAToast(tMessage: String) {
        Toast.makeText(this, tMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sign_in_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.password_reset) {
            startActivity(Intent(this, PasswordReset::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    companion object{
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id!!)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val email = user?.email
                    makeAToast("Sign In: Successful: $email")
                    startActivity(Intent(this, StashList::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    makeAToast("Sign In Failed")

                }

            }
    }
}
