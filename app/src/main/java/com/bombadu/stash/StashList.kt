package com.bombadu.stash

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bombadu.stash.model.Links
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_stash_list.*
import kotlinx.android.synthetic.main.date_range_layout.*
import kotlinx.android.synthetic.main.web_view_fragment.*
import org.nibor.autolink.LinkExtractor
import org.nibor.autolink.LinkType
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class StashList : AppCompatActivity(), StashAdapter.ItemClickCallback {

    private var rootRef = FirebaseDatabase.getInstance().reference
    private var listData = mutableListOf<Links>()
    private lateinit var auth: FirebaseAuth
    private var urlListRef: DatabaseReference? = null
    private var show = false
    private var nagCountMax = 3
    private var nagCount = 0
    private var isTwoPane: Boolean = false
    private var lastWebUrl: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stash_list2)
        isTwoPane = findViewById<LinearLayout>(R.id.activity_web_view_frag) != null

        requestedOrientation = if (isTwoPane) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        auth = FirebaseAuth.getInstance()
        loadPrefs()

        val uid = auth.currentUser?.uid
        val usersRef = rootRef.child("users")
        val userDataRef = usersRef.child(uid.toString())
        urlListRef = userDataRef.child("url_list")
        val editText = findViewById<EditText>(R.id.add_edit_text)
        val addLinkButton = findViewById<Button>(R.id.add_link_button)
        val timeSpan: Long = getRangeInTime(3650)


        getFBData(timeSpan)

        val intent = intent
        var myUrl = intent.getStringExtra("url_key")


        if (myUrl != null) {
            myUrl = extractUrl(myUrl)
            val timeStamp = getTimeStamp()
            val taskMap: MutableMap<String, Any> = HashMap()
            taskMap["time_stamp"] = timeStamp
            taskMap["url"] = myUrl as String
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

        if (isTwoPane){
            openInBrowserButton.setOnClickListener {
                myUrl = web_view_fr.url
                openBrowser(myUrl)
            }
        }


    }

    private fun extractUrl(myUrl: String?): String? {
        val linkExtractor = LinkExtractor.builder()
            .linkTypes(EnumSet.of(LinkType.URL, LinkType.WWW, LinkType.EMAIL))
            .build()
        val links = linkExtractor.extractLinks(myUrl)
        val link = links.iterator().next()
        link.type
        link.beginIndex
        link.endIndex
        return myUrl?.substring(link.beginIndex, link.endIndex)


    }


    private fun savePrefs() {
        val myPrefs = getSharedPreferences("prefs_key", Context.MODE_PRIVATE)
        val myEditor = myPrefs.edit()
        myEditor.putInt("prefs_key", nagCount)
        myEditor.apply()

    }

    private fun loadPrefs() {
        val myPrefs = getSharedPreferences("prefs_key", Context.MODE_PRIVATE)
        nagCount = myPrefs.getInt("prefs_key", 0)
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

                    lastWebUrl = url


                }

                if (isTwoPane) {
                    if (lastWebUrl != "") {
                        web_view_fr.settings.javaScriptEnabled = true
                        web_view_fr.loadUrl(lastWebUrl)

                    }


                }

                if (listData.isEmpty() && nagCount < nagCountMax) {
                    showEmptyListDialog()
                    nagCount++
                    savePrefs()
                }

                val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
                recyclerView.layoutManager = LinearLayoutManager(this@StashList)
                val stashAdapter = StashAdapter(listData)
                recyclerView.adapter = stashAdapter
                stashAdapter.setItemClickCallback(this@StashList)
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
            builder.setTitle(getString(R.string.stash_version))
            builder.setMessage("Build Date: 2-17-2020\nby Michael May\nBombadu")
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
            startActivity(Intent(this, Auth2::class.java))
            finish()
        }

        if (item.itemId == R.id.date_range) {
            showDateRangeDialog()
        }

        if (item.itemId == R.id.help) {
            startActivity(Intent(this, Help::class.java))
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
                    showingTextView.text = getString(R.string.showing_past_24_hours)


                }
                "Past Week" -> {
                    timeSpan = getRangeInTime(7)
                    getFBData(timeSpan)
                    showingTextView.text = getString(R.string.showing_past_week)

                }
                "Past Month" -> {
                    timeSpan = getRangeInTime(30)
                    getFBData(timeSpan)
                    showingTextView.text = getString(R.string.showing_past_month)

                }
                else -> {
                    timeSpan = getRangeInTime(3650) // 10 years - all
                    getFBData(timeSpan)
                    showingTextView.text = getString(R.string.showing_all)

                }
            }

            dialog.cancel()
        }

        dialog.show()
    }


    private fun showLinkEntryLayout() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein)
        add_link_layout.startAnimation(fadeIn)
        show = true
        add_link_layout.visibility = View.VISIBLE
    }
    private fun hideLinkEntryLayout() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout)
        add_link_layout.startAnimation(fadeOut)
        showingTextView.startAnimation(fadeIn)
        show = false
        add_link_layout.visibility = View.GONE

    }


    override fun onItemClick(p: Int) {
        val revPos = listData.size - (p + 1)
        val item = listData[revPos]
        val myUrl = item.webUrl
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib.vibrate(50)

        if(isTwoPane) {
            web_view_fr.settings.javaScriptEnabled = true
            web_view_fr.loadUrl(myUrl)



        } else {
            openBrowser(myUrl)
        }
    }

    private fun openBrowser(myUrl: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(myUrl)
        startActivity(intent)
    }


}
