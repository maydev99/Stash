package com.bombadu.stash

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bombadu.stash.model.Links
import com.freesoulapps.preview.android.Preview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.stash_card.view.*

class StashAdapter(private val listData: List<Links>) :
    RecyclerView.Adapter<StashAdapter.ViewHolder>() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var rootRef = FirebaseDatabase.getInstance().reference
    private var uid = auth.currentUser?.uid
    private var urlListRef = rootRef.child("users").child(uid.toString()).child("url_list")
    private var itemClickCallback: ItemClickCallback? = null

    internal interface ItemClickCallback {
        fun onItemClick(p: Int)
    }

    internal fun setItemClickCallback(inItemClickCallback: ItemClickCallback) {
        this.itemClickCallback = inItemClickCallback
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stash_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val revPos =
            listData.size - (position + 1)//Reverses the position so newest posts are at the top
        holder.bindItems(listData[revPos])

    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        Preview.PreviewListener, View.OnClickListener {
        var webUrl: String? = null
        var urlKey: String? = null
        var timeStamp: String? = null

        fun bindItems(links: Links) {
            webUrl = links.webUrl
            urlKey = links.urlKey
            timeStamp = links.dateTime
            val previewView = itemView.findViewById<Preview>(R.id.preview_view)
            val dateTimeTextView = itemView.findViewById<TextView>(R.id.dateTimeTextView)


            dateTimeTextView.text = timeStamp
            previewView.setListener(this)
            previewView.setData(webUrl)


        }

        override fun onDataReady(preview: Preview?) {
            preview?.setMessage(preview.link)
        }


        init {
            itemView.share_image_view.setOnClickListener {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, webUrl)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                itemView.context.startActivity(shareIntent)
            }

            itemView.card_container.setOnClickListener(this)

            itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(itemView.context)
                builder.setTitle("Delete this item")
                builder.setMessage("Are you sure?")
                builder.setIcon(R.mipmap.ic_launcher_round)
                builder.setPositiveButton("delete") { _, _ ->
                    Toast.makeText(itemView.context, "Deleted", Toast.LENGTH_SHORT).show()
                    urlListRef.child(urlKey.toString()).removeValue()
                }

                builder.setNegativeButton("cancel") { _, _ ->
                    //Toast.makeText(itemView.context, "Deleted",Toast.LENGTH_SHORT).show()
                }
                builder.show()

                return@setOnLongClickListener true
            }


        }

        override fun onClick(p0: View?) {

            itemClickCallback!!.onItemClick(adapterPosition)
        }


    }


}