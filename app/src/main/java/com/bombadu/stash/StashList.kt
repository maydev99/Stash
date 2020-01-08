package com.bombadu.stash

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bombadu.stash.model.Links
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StashList : AppCompatActivity() {

    private var rootRef = FirebaseDatabase.getInstance().reference
    private var urlRef = rootRef.child("url")
    private var listData = mutableListOf<Links>()
    private val version = "1.0"
    private val buildDate = "1-6-2020"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stash_list)

        getFBData()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val editText = findViewById<EditText>(R.id.add_edit_text)
        val addLinkButton = findViewById<Button>(R.id.add_link_button)

        val receivedIntent = intent
        val receivedAction = receivedIntent.action
        val receivedType = receivedIntent.type
        if (receivedAction.equals(Intent.ACTION_SEND)) {
            if (receivedType != null) {
                if (receivedType.startsWith("text/")) {
                    val urlText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT)
                    println(urlText)
                    //urlList.add(urlText)
                    urlRef.push().setValue(urlText)


                }
            }
        }


        addLinkButton.setOnClickListener {

            val newLink = editText.text.toString()
            if (newLink.contains("https") || (newLink.contains("https")) && (!newLink.contains(""))) {
                urlRef.push().setValue(newLink)
                val addLinkLayout = findViewById<LinearLayout>(R.id.add_link_layout)
                editText.text = null
                addLinkLayout.visibility = View.GONE

            } else {

                Toast.makeText(this, "not a valid url", Toast.LENGTH_SHORT).show()
            }


        }


    }

    private fun getFBData() {

        val urlListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //nothing
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listData.clear()
                for (item in dataSnapshot.children) {
                    val url = item.value.toString()
                    val key = item.key.toString()
                    listData.add(Links(url,key))
                    listData.reverse()

                }
                //println("URLS: $urlList")

                val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
                recyclerView.layoutManager = LinearLayoutManager(this@StashList)
                val stashAdapter = StashAdapter(listData)
                recyclerView.adapter = stashAdapter

                stashAdapter.notifyDataSetChanged()


            }


        }

        urlRef.addValueEventListener(urlListener)


    }

    override fun onStart() {
        super.onStart()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.stash_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.about){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Stash v$version")
            builder.setMessage("Build Date: $buildDate\nbyMichael May\nBombadu")
            builder.setIcon(R.mipmap.ic_launcher_round)
            builder.show()


        }


        if (item.itemId == R.id.add_link) {
            val addLinkLayout = findViewById<LinearLayout>(R.id.add_link_layout)
            addLinkLayout.visibility = if (addLinkLayout.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        return super.onOptionsItemSelected(item)
    }


}
