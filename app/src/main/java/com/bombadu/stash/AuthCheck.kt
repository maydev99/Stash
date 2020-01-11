package com.bombadu.stash

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class AuthCheck : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var recUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_check)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        auth = FirebaseAuth.getInstance()

        //Receives the Intent containing the url that was shared
        val receivedIntent = intent
        val receivedAction = receivedIntent.action
        val receivedType = receivedIntent.type
        if (receivedAction.equals(Intent.ACTION_SEND)) {
            if (receivedType != null) {
                if (receivedType.startsWith("text/")) {
                    recUrl = receivedIntent.getStringExtra(Intent.EXTRA_TEXT)
                    println("URLAUTHCHECK: $recUrl")




                }
            }


        }

        //Check to see if User is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, StashList::class.java)
            if(recUrl != null) {
                //If a url has been shared is get passed to StashList Activity
                intent.putExtra("url_key", recUrl)
            }

            startActivity(intent)
            finish()

            println("Authenticated")

        } else {
            startActivity(Intent(this, Auth::class.java))
            finish()
            println("Authentication Failed")
        }


    }


}
