package com.bombadu.stash

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
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
        //checkConnection()
        fetchSharedLink()
        checkIfUserIsAuthenticated()
    }

    private fun checkConnection() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (!connectivityManager.isDefaultNetworkActive) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("No Internet Connection")
            builder.setMessage("Please check your connection settings")
            builder.setIcon(R.drawable.noconnection)
            builder.setCancelable(false)
            builder.setPositiveButton("settings") { _, _ ->
                this.startActivity(Intent(Settings.ACTION_SETTINGS))
            }

            builder.setNegativeButton("close") { _, _ ->
                //Closes Dialog
            }
            builder.show()
        } else {
            fetchSharedLink()
            checkIfUserIsAuthenticated()
        }

    }

    private fun checkIfUserIsAuthenticated() {
        //Check to see if User is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, StashList::class.java)
            if (recUrl != null) {
                //If a url has been shared is get passed to StashList Activity
                intent.putExtra("url_key", recUrl)
            }

            startActivity(intent)
            finish()

            println("Authenticated")

        } else {
            startActivity(Intent(this, Auth2::class.java))
            finish()
            println("Authentication Failed")
        }
    }

    private fun fetchSharedLink() {
        //Receives the Intent containing the url that was shared
        val receivedIntent = intent
        val receivedAction = receivedIntent.action
        val receivedType = receivedIntent.type
        if (receivedAction.equals(Intent.ACTION_SEND)) {
            if (receivedType != null) {
                if (receivedType.startsWith("text/")) {
                    recUrl = receivedIntent.getStringExtra(Intent.EXTRA_TEXT)
                    println("URL CHECK: $recUrl")


                }
            }


        }
    }

    override fun onStart() {
        super.onStart()
        checkConnection()


    }


}
