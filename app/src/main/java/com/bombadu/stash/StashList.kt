package com.bombadu.stash

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bombadu.stash.model.Links
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_stash_list_entry_bar.*
import kotlinx.android.synthetic.main.date_range_layout.*
import kotlinx.android.synthetic.main.empty_list_dialog_layout.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class StashList : AppCompatActivity() {

    private var rootRef = FirebaseDatabase.getInstance().reference
    private var listData = mutableListOf<Links>()
    private val version = "1.0"
    private val buildDate = "1-6-2020"
    private lateinit var auth: FirebaseAuth
    private var urlListRef: DatabaseReference? = null
    private var show = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stash_list)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        val usersRef = rootRef.child("users")
        val userDataRef = usersRef.child(uid.toString())
        urlListRef = userDataRef.child("url_list")
        val editText = findViewById<EditText>(R.id.add_edit_text)
        val addLinkButton = findViewById<Button>(R.id.add_link_button)
        val timeSpan: Long = getRangeInTime(3650)

        getFBData(timeSpan)

        val intent = intent
        val myUrl = intent.getStringExtra("url_key")


        if (myUrl != null) {
            val timeStamp = getTimeStamp()
            val taskMap: MutableMap<String, Any> = HashMap()
            taskMap["time_stamp"] = timeStamp
            taskMap["url"] = myUrl
            urlListRef?.push()?.updateChildren(taskMap)
        }



        addLinkButton.setOnClickListener {

            val newLink = editText.text.toString()
            if (newLink.contains("http")) {
                val timeStamp = getTimeStamp()
                val taskMap: MutableMap<String, Any> = HashMap()
                taskMap["time_stamp"] = timeStamp
                taskMap["url"] = newLink
                urlListRef?.push()?.updateChildren(taskMap)
                editText.text = null


            } else {

                Toast.makeText(this, "not a valid url", Toast.LENGTH_SHORT).show()
            }


        }


    }

    private fun getRangeInTime(dateRange: Long): Long {
        val timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        //val timeStampL = timeStamp.toLong()
        val elapsedTime: Long = when {
            dateRange <= 1 -> {
                (TimeUnit.DAYS.toMillis(1) / 1000)
            }
            dateRange <= 7 -> {
                (TimeUnit.DAYS.toMillis(7) / 1000)
            }
            dateRange <= 30 -> {
                (TimeUnit.DAYS.toMillis(30) / 1000)
            }
            else -> {
                (TimeUnit.DAYS.toMillis(1095) / 1000)
            }
        }

        return timeStamp - elapsedTime

    }

    //Timestamp is saved to Firebase when entered into db and formatted to local time when retrieved
    private fun getTimeStamp(): String {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString() + ""
    }

    private fun getFBData(dateRange: Long) {

        val urlListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //nothing
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listData.clear()
                for (item in dataSnapshot.children) {
                    val key = item.key.toString()
                    val url = dataSnapshot.child(key).child("url").value.toString()
                    val timeStamp: String =
                        dataSnapshot.child(key).child("time_stamp").value as String
                    val timeStampL: Long = timeStamp.toLong()
                    if (timeStampL >= dateRange) {
                        val dateTime: String = localDateTime(timeStamp) as String
                        listData.add(Links(url, key, dateTime))
                    }




                }

                if(listData.isEmpty()) {
                    showEmptyListDialog()
                }


                val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
                recyclerView.layoutManager = LinearLayoutManager(this@StashList)
                val stashAdapter = StashAdapter(listData)
                recyclerView.adapter = stashAdapter

                stashAdapter.notifyDataSetChanged()


            }


        }

        urlListRef?.addValueEventListener(urlListener)


    }

    private fun showEmptyListDialog() {
        val emptyListDialog = Dialog(this)
        emptyListDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        emptyListDialog.setCancelable(false)
        emptyListDialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        emptyListDialog.setContentView(R.layout.empty_list_dialog_layout)
        emptyListDialog.show()

        val closeTextView = emptyListDialog.findViewById<TextView>(R.id.closeTextView)
        closeTextView.setOnClickListener {
            emptyListDialog.cancel()
        }

    }

    private fun localDateTime(timeStamp: String): Any {
        val calendar = Calendar.getInstance()
        val tz = calendar.timeZone
        val sdf = SimpleDateFormat("MM.dd.yyy hh:mm:ss a", Locale.getDefault())
        sdf.timeZone = tz
        val tsLong = timeStamp.toLong()
        return sdf.format(Date(tsLong * 1000))

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.stash_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.about) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Stash v$version")
            builder.setMessage("Build Date: $buildDate\nby Michael May\nBombadu")
            builder.setIcon(R.mipmap.ic_launcher_round)
            builder.show()


        }


        if (item.itemId == R.id.add_link) {

            if (show) {
                hideLinkEntryLayout()
            } else {
                showLinkEntryLayout()
            }

        }


        if (item.itemId == R.id.sign_out) {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, Auth::class.java))
            finish()
        }

        if (item.itemId == R.id.date_range) {
            showDateRangeDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDateRangeDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.setContentView(R.layout.date_range_layout)
        dialog.radio_group.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton: RadioButton = dialog.findViewById(checkedId)
            val selected: String = selectedRadioButton.text.toString()
            //println("RB: $selected")

            println("SELECTED: $selected")

            val timeSpan: Long
            //var myChecked = "all"
            when (selected) {
                "Past 24 Hours" -> {
                    timeSpan = getRangeInTime(1)
                    getFBData(timeSpan)


                }
                "Past Week" -> {
                    timeSpan = getRangeInTime(7)
                    getFBData(timeSpan)

                }
                "Past Month" -> {
                    timeSpan = getRangeInTime(30)
                    getFBData(timeSpan)

                }
                else -> {
                    timeSpan = getRangeInTime(3650) // 10 years - all
                    getFBData(timeSpan)

                }
            }


            dialog.cancel()
        }

        dialog.show()
    }


    private fun showLinkEntryLayout() {
        show = true

        val constraintSet = ConstraintSet()
        constraintSet.clone(this, R.layout.activity_stash_list_entry_bar)

        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(1.0f)
        transition.duration = 1200

        TransitionManager.beginDelayedTransition(constraint, transition)
        constraintSet.applyTo(constraint)
    }

    private fun hideLinkEntryLayout() {

        show = false

        val constraintSet = ConstraintSet()
        constraintSet.clone(this, R.layout.activity_stash_list)

        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(1.0f)
        transition.duration = 1200

        TransitionManager.beginDelayedTransition(constraint, transition)
        constraintSet.applyTo(constraint)

    }


}
