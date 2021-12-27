package com.example.finalproject.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.model.Message
import com.example.finalproject.R
import com.example.finalproject.safe.TimeLogic

class ChatBoxAdapter internal constructor(context: Context?, data: List<Message>) :
    RecyclerView.Adapter<ChatBoxAdapter.ViewHolder>() {
    private val mData: List<Message> = data
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = if (viewType == USER_TEXT) {
            mInflater.inflate(R.layout.chatbox_user, parent, false)
        } else {
            mInflater.inflate(R.layout.chatbox_other, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = mData[position]
        holder.lastMessage.text = animal.messageContent
        holder.lastMessageTime.text = TimeLogic.CustomTimeFormat(animal.messageTime)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mData[position].isUser) USER_TEXT else OTHER_TEXT
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var lastMessage: TextView = itemView.findViewById(R.id.messageContent)
        var lastMessageTime: TextView = itemView.findViewById(R.id.messageTime)
        override fun onClick(view: View) {}

        init {
            itemView.setOnClickListener(this)
        }
    }

    fun getItem(id: Int): Message {
        return mData[id]
    }

    companion object {
        private const val USER_TEXT = 1
        private const val OTHER_TEXT = 2
    }

}