package com.bombadu.stash

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bombadu.stash.model.Links
import com.freesoulapps.preview.android.Preview
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.coroutineContext

class StashAdapter(private val listData: List<Links>) : RecyclerView.Adapter<StashAdapter.ViewHolder>(){

    private var rootRef = FirebaseDatabase.getInstance().reference
    private var urlRef = rootRef.child("url")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.stash_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(listData[position])
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        Preview.PreviewListener {
        var webUrl: String? = null
        var urlKey: String? = null

        fun bindItems(links: Links) {
            webUrl= links.webUrl
            urlKey = links.urlKey
            val previewView = itemView.findViewById<Preview>(R.id.preview_view)
            previewView.setListener(this)
            previewView.setData(webUrl)


        }

        override fun onDataReady(preview: Preview?) {
            preview?.setMessage(preview.link)
        }


        init {
            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(webUrl)
                itemView.context.startActivity(intent)
            }

            itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(itemView.context)
                builder.setTitle("Delete this item")
                builder.setMessage("Are you sure?")
                builder.setIcon(R.mipmap.ic_launcher_round)
                builder.setPositiveButton("delete") {dialog, which ->
                    Toast.makeText(itemView.context, "Deleted",Toast.LENGTH_SHORT).show()
                    urlRef.child(urlKey.toString()).removeValue()
                }

                builder.setNegativeButton("cancel") {dialog, which ->
                    //Toast.makeText(itemView.context, "Deleted",Toast.LENGTH_SHORT).show()
                }
                builder.show()

                return@setOnLongClickListener true
            }

        }
    }


}