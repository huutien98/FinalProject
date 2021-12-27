package com.example.finalproject.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.model.UserListComponent
import com.example.finalproject.safe.TimeLogic
import com.example.finalproject.view.ChatScreenActivity

class UserListAdapter internal constructor(context: Context, data: List<UserListComponent>) :
    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {
    private val mData: List<UserListComponent> = data
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val context: Context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.chatlist_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = mData[position]
        holder.chatName.text = animal.chatName
        holder.lastMessage.text = animal.lastMessage
        holder.lastMassageTime.text = TimeLogic.CustomTimeFormat(animal.lastTextTime)
        holder.userProfile.setImageBitmap(
            BitmapFactory.decodeByteArray(
                animal.profilePic,
                0,
                animal.profilePic.size
            )
        )
        holder.linearLayout.setOnClickListener {
            val intent = Intent(context, ChatScreenActivity::class.java)
            intent.putExtra("username", mData[position].chatUsername)
            intent.putExtra("name", mData[position].chatName)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var chatName: TextView = itemView.findViewById(R.id.recycler_UserTitle)
        var lastMessage: TextView = itemView.findViewById(R.id.recycler_lastText)
        var lastMassageTime: TextView = itemView.findViewById(R.id.recycler_textTime)
        var userProfile: ImageView = itemView.findViewById(R.id.profileImage)
        var linearLayout: LinearLayout = itemView.findViewById(R.id.linLayout)
        override fun onClick(view: View) {}

        init {
            itemView.setOnClickListener(this)
        }
    }

    fun getItem(id: Int): UserListComponent {
        return mData[id]
    }

}