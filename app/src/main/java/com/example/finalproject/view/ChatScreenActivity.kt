package com.example.finalproject.view

import android.annotation.SuppressLint
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.*
import com.google.firebase.database.FirebaseDatabase
import com.example.finalproject.adapter.ChatBoxAdapter
import com.example.finalproject.custom.CustomApplication
import com.example.finalproject.database.ChatlistDBHelper
import com.example.finalproject.database.UserListDBHelper
import com.example.finalproject.firebase.FirebaseMessage
import com.example.finalproject.model.Message
import com.example.finalproject.safe.RSAEncyption
import java.io.IOException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.*

class ChatScreenActivity : AppCompatActivity() {
    var adapter: ChatBoxAdapter? = null
    var send_btn: ImageButton? = null
    var handler: Handler? = null
    var sendText: EditText? = null
    var recyclerView: RecyclerView? = null
    var back_btn: ImageButton? = null
    var profileImage: ImageView? = null
    var nameView: TextView? = null
    var usernameView: TextView? = null
    var name: String? = null
    var username: String? = null
    var tableName: String? = null
    var profileImageData: Bitmap? = null

    var publicKey: PublicKey? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)
        val messageArrayList = ArrayList<Message>()
        val intent = intent
        name = intent.getStringExtra("name")
        username = intent.getStringExtra("username")


        val cesr = (application as CustomApplication).userListDBHelper?.getUser(username.toString())
        if (cesr?.count != 0) {
            while (cesr?.moveToNext() == true) {
                name = cesr.getString(1)
                val b = cesr.getBlob(6)
                profileImageData = BitmapFactory.decodeByteArray(b, 0, b.size)
                try {
                    publicKey = (application as CustomApplication).kf?.generatePublic(X509EncodedKeySpec(cesr.getBlob(7)))
                } catch (e: InvalidKeySpecException) {
                    e.printStackTrace()
                }
            }
        }

        tableName = "Fleet_$username"
        val myRef = FirebaseDatabase.getInstance().getReference("messages").child(username.toString())
        val userListDbHelper = UserListDBHelper(applicationContext)
        val chatlistDBHelper = ChatlistDBHelper(applicationContext)
        chatlistDBHelper.addUser(tableName.toString())
        val csr = chatlistDBHelper.getAllMessages(tableName.toString())
        if (csr.count > 0) {
            while (csr.moveToNext()) {
                val msgId = csr.getInt(0)
                val isUser = csr.getInt(1) == 1
                var msg: String? = null
                try {
                    msg = RSAEncyption.decryptData(
                        csr.getBlob(2),
                        (application as CustomApplication).user_PrivateKey
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val timing = csr.getString(3)
                messageArrayList.add(Message(msgId, isUser, msg.toString(), timing))
            }
        }

        recyclerView = findViewById(R.id.ChatScreenRecycler)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = ChatBoxAdapter(this, messageArrayList)
        recyclerView?.adapter = adapter
        recyclerView?.scrollToPosition(messageArrayList.size - 1)
        back_btn = findViewById(R.id.back_btn)
        profileImage = findViewById(R.id.profileImage)
        usernameView = findViewById(R.id.user_username)
        nameView = findViewById(R.id.user_name)
        sendText = findViewById(R.id.username_input)
        send_btn = findViewById(R.id.sendText_btn)
        handler = Handler()
        handler?.postDelayed(object : Runnable {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                val csr: Cursor = try {
                    chatlistDBHelper.getNewMessages(
                        tableName.toString(),
                        messageArrayList[messageArrayList.size - 1].msgId
                    )
                } catch (erer: Exception) {
                    chatlistDBHelper.getNewMessages(tableName.toString(), 0)
                }
                if (csr.count > 0) {
                    while (csr.moveToNext()) {
                        val msgId = csr.getInt(0)
                        val isUser = csr.getInt(1) == 1
                        var msg: String? = null
                        try {
                            msg = RSAEncyption.decryptData(
                                csr.getBlob(2),
                                (application as CustomApplication).user_PrivateKey
                            )
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val timing = csr.getString(3)
                        messageArrayList.add(Message(msgId, isUser, msg.toString(), timing))
                    }
                    recyclerView?.adapter?.notifyDataSetChanged()
                    recyclerView?.scrollToPosition(messageArrayList.size - 1)
                }
                handler?.postDelayed(this, 1000)
            }
        }, 1000)
        back_btn?.setOnClickListener({ finish() })
        profileImage?.setImageBitmap(profileImageData)
        usernameView?.text = "@$username"
        nameView?.text = name
        send_btn?.setOnClickListener(object : View.OnClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onClick(v: View) {
                if (sendText?.text.toString().isNotEmpty()) {
                    try {
                        val fb = FirebaseMessage(
                            (application as CustomApplication).username,
                            Base64.encodeToString(
                                RSAEncyption.encryptData(
                                    sendText?.text.toString(), publicKey
                                ), Base64.DEFAULT
                            )
                        )
                        myRef.push().setValue(fb)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        chatlistDBHelper.insertMessage(
                            tableName,
                            RSAEncyption.encryptData(
                                sendText?.text.toString(),
                                (application as CustomApplication).user_PublicKey
                            ),
                            true
                        )

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    try {
                        userListDbHelper.updateLastText(
                            username.toString(),
                            RSAEncyption.encryptData(
                                sendText?.text.toString(),
                                (application as CustomApplication).user_PublicKey
                            )
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    try {
                        updateChats()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    recyclerView?.adapter?.notifyDataSetChanged()
                    recyclerView?.scrollToPosition(messageArrayList.size - 1)
                    sendText?.setText("")
                }
            }

            @Throws(IOException::class)
            private fun updateChats() {
                var _id = 0
                try {
                    _id = messageArrayList[messageArrayList.size - 1].msgId
                } catch (e: Exception) {
                }
                val csr = chatlistDBHelper.getNewMessages(tableName.toString(), _id)
                if (csr.count > 0) {
                    while (csr.moveToNext()) {
                        val msgId = csr.getInt(0)
                        val isUser = csr.getInt(1) == 1
                        val msg = RSAEncyption.decryptData(
                            csr.getBlob(2),
                            (application as CustomApplication).user_PrivateKey
                        )
                        val timing = csr.getString(3)
                        messageArrayList.add(Message(msgId, isUser, msg, timing))
                    }
                }
            }

            private fun gettingAllUserDetails() {
                val csr = userListDbHelper.allUsers
                if (csr.count > 0) {
                    while (csr.moveToNext()) {
                        var str = ""
                        str += """
                            
                            ID: ${csr.getInt(0)}
                            """.trimIndent()
                        str += """
                            
                            NAME: ${csr.getString(1)}
                            """.trimIndent()
                        str += """
                            
                            USERNAME: ${csr.getString(2)}
                            """.trimIndent()
                        str += """
                            
                            TABLE NAME: ${csr.getString(3)}
                            """.trimIndent()
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}