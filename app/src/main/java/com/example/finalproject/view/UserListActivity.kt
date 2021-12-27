package com.example.finalproject.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.View
import android.view.View.OnLongClickListener
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.example.finalproject.adapter.UserListAdapter
import com.example.finalproject.custom.CustomApplication
import com.example.finalproject.database.UserListDBHelper
import com.example.finalproject.model.User
import com.example.finalproject.model.UserListComponent
import com.example.finalproject.safe.RSAEncyption
import java.util.*

class UserListActivity : AppCompatActivity() {
    private var adapter: UserListAdapter? = null
    private var User_profile_image: ImageView? = null
    var name: String? = null
    var username: String? = null
    private var email_id: String? = null
    var username_view: TextView? = null
    private var name_view: TextView? = null
    private var email_id_view: TextView? = null
    var recyclerView: RecyclerView? = null
    var floatingActionButton: FloatingActionButton? = null
    var handler: Handler? = null
    var newUserUsername: String? = null
    var btn_code = 0
    var logoIcon: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlist)
        logoIcon = findViewById(R.id.logoicon2)
        logoIcon?.setOnLongClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this@UserListActivity, "You've been signed out.", Toast.LENGTH_SHORT)
                .show()
            false
        }
        val sharedpreferences = getSharedPreferences("personal_details", MODE_PRIVATE)
        name = sharedpreferences.getString("name", "")
        username = sharedpreferences.getString("username", "")
        (this.application as CustomApplication).username = username.toString()
        (this.application as CustomApplication).startIncomingMsgs()

        email_id = sharedpreferences.getString("email_id", "")
        val previouslyEncodedImage = sharedpreferences.getString("image_data", "")
        User_profile_image = findViewById(R.id.profileImage)
        name_view = findViewById(R.id.user_name)
        email_id_view = findViewById(R.id.user_email)
        username_view = findViewById(R.id.user_username)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        floatingActionButton?.setOnClickListener {
            floatingActionButton?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate))

             val dialog = Dialog(this)
            dialog.setContentView(R.layout.adduser_fragment)
            dialog.setTitle("This is my custom dialog box")
            dialog.setCancelable(true)
            dialog.setOnCancelListener { dialog1: DialogInterface? ->
                floatingActionButton?.startAnimation(
                    AnimationUtils.loadAnimation(
                        applicationContext, R.anim.rev_rotate
                    )
                )
                val lp = window.attributes
                lp.alpha = 1f
                window.attributes = lp
            }
            val lp = window.attributes
            lp.alpha = 0.6f
            window.attributes = lp
            btn_code = 0
            val searchbar: EditText = dialog.findViewById(R.id.search_username)
            val userPrompt: TextView = dialog.findViewById(R.id.userPrompt)
            val search_btn: Button = dialog.findViewById(R.id.addUserButton)
            val loading_spinner: ProgressBar = dialog.findViewById(R.id.loading_spinner)
            loading_spinner.visibility = View.INVISIBLE
            search_btn.setOnClickListener { ve: View? ->
                newUserUsername = searchbar.text.toString()
                if (newUserUsername!!.isNotEmpty()) {
                    userPrompt.visibility = View.INVISIBLE
                    loading_spinner.visibility = View.VISIBLE
                    val database = FirebaseDatabase.getInstance()
                    val myRef = database.getReference("Users")
                    if (btn_code == 0) {
                        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                loading_spinner.visibility = View.INVISIBLE
                                if (snapshot.hasChild(newUserUsername!!)) {
                                    userPrompt.setTextColor(resources.getColor(R.color.green_success))
                                    userPrompt.text = "User found"
                                    userPrompt.visibility = View.VISIBLE
                                    btn_code = 1
                                    search_btn.text = "ADD"
                                } else {
                                    userPrompt.setTextColor(resources.getColor(R.color.red_error))
                                    userPrompt.text = "User not found"
                                    userPrompt.visibility = View.VISIBLE
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    } else {
                        myRef.child(newUserUsername.toString())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val tempUser: User? =
                                        snapshot.getValue(User::class.java)
                                    val storage = FirebaseStorage.getInstance()
                                    val storageRef = storage.reference
                                    val imagesRef = storageRef.child(
                                        "images/" + tempUser?.username.toString() + ".jpg"
                                    )
                                    imagesRef.getBytes(Long.MAX_VALUE)
                                        .addOnSuccessListener { bytes: ByteArray? ->
                                            val publicKeyRef = storageRef.child(
                                                "Public_Keys/" + tempUser?.username
                                                    .toString() + ".key"
                                            )
                                            publicKeyRef.getBytes(Long.MAX_VALUE)
                                                .addOnSuccessListener { bytes2: ByteArray? ->
                                                    val userListDBHelper = UserListDBHelper(
                                                        applicationContext
                                                    )
                                                    userListDBHelper.insertUser(
                                                        tempUser?.name,
                                                        tempUser?.username.toString(),
                                                        bytes,
                                                        (application as CustomApplication).emptyMsg,
                                                        bytes2
                                                    )
                                                    dialog.dismiss()
                                                    val lp = window.attributes
                                                    lp.alpha = 1f
                                                    window.attributes = lp
                                                }
                                        }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }
            }
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialog.show()
        }
        name_view?.text = name
        username_view?.text = username
        email_id_view?.text = email_id
        if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
            val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
            User_profile_image?.setImageBitmap(bitmap)
        }
        val chatListMembers = ArrayList<UserListComponent>()
        recyclerView = findViewById(R.id.chatlist_recyclerview)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = UserListAdapter(this, chatListMembers)
        recyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView?.adapter = adapter

        val userListDBHelper = UserListDBHelper(this)
        handler = Handler()
        handler?.postDelayed(object : Runnable {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                val csr = userListDBHelper.allUsers
                if (csr.count > 0) {
                    chatListMembers.clear()
                    while (csr.moveToNext()) {
                        val name = csr.getString(1)
                        val username = csr.getString(2)
                        var last_message: String? = null
                        try {
                            last_message = RSAEncyption.decryptData(
                                csr.getBlob(4),
                                (application as CustomApplication).user_PrivateKey
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        val last_message_time = csr.getString(5)
                        val profile_image = csr.getBlob(6)
                        chatListMembers.add(
                            UserListComponent(
                                name,
                                username,
                                last_message.toString(),
                                profile_image,
                                last_message_time
                            )
                        )
                    }
                    recyclerView?.adapter?.notifyDataSetChanged()
                }
                handler?.postDelayed(this, 3000)
            }
        }, 0)
    }
}