package com.example.finalproject.custom

import android.app.Application
import android.util.Base64
import com.example.finalproject.database.ChatlistDBHelper
import com.example.finalproject.database.UserListDBHelper
import com.example.finalproject.firebase.FirebaseMessage
import com.example.finalproject.model.User
import com.example.finalproject.safe.RSAEncyption
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class CustomApplication : Application() {
    var chatlistDBHelper: ChatlistDBHelper? = null
    var userListDBHelper: UserListDBHelper? = null
    var username = ""
    var user_PrivateKey: PrivateKey? = null
    var user_PublicKey: PublicKey? = null
    var emptyMsg: ByteArray? = null
    var kf: KeyFactory? = null
    override fun onCreate() {
        super.onCreate()
        try {
            kf = KeyFactory.getInstance("RSA")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        val keySharedPrefs = getSharedPreferences("Personal_keys", MODE_PRIVATE)
        val encodedPrivateKey = keySharedPrefs.getString("privateKey", "")
        val encodedPublicKey = keySharedPrefs.getString("publicKey", "")
        if (!encodedPrivateKey.equals("", ignoreCase = true)) {
            val b = Base64.decode(encodedPrivateKey, Base64.DEFAULT)
            try {
                user_PrivateKey = kf?.generatePrivate(PKCS8EncodedKeySpec(b))
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            }
        }
        if (!encodedPublicKey.equals("", ignoreCase = true)) {
            val b = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            try {
                user_PublicKey = kf?.generatePublic(X509EncodedKeySpec(b))
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            }
        }

         try {
            emptyMsg = RSAEncyption.encryptData("", user_PublicKey)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        chatlistDBHelper = ChatlistDBHelper(applicationContext)
        userListDBHelper = UserListDBHelper(applicationContext)

     }

    fun startIncomingMsgs() {
         val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("messages").child(username)
        myRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val fb: FirebaseMessage? = snapshot.getValue(FirebaseMessage::class.java)

                 if (userListDBHelper?.getUser(fb?.username.toString())?.count!! > 0) {
                    try {
                        chatlistDBHelper?.insertMessage(
                            "Fleet_" + fb?.username,
                            Base64.decode(fb?.message, Base64.DEFAULT),
                            false
                        )
                         fb?.username?.let {
                            userListDBHelper?.updateLastText(
                                it,
                                Base64.decode(fb?.message, Base64.DEFAULT)
                            )
                        }
                     } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    chatlistDBHelper?.addUser("Fleet_" + fb?.username)
                    try {
                        chatlistDBHelper?.insertMessage(
                            "Fleet_" + fb?.username,
                            Base64.decode(fb?.message, Base64.DEFAULT),
                            false
                        )
                     } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val database = FirebaseDatabase.getInstance()
                    val myRef = database.getReference("Users").child(fb?.username.toString())
                    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val tempUser: User? = snapshot.getValue(User::class.java)
                            val storage = FirebaseStorage.getInstance()
                            val storageRef = storage.reference
                            val imagesRef = storageRef.child(
                                "images/" + tempUser?.username.toString() + ".jpg"
                            )
                            imagesRef.getBytes(Long.MAX_VALUE)
                                .addOnSuccessListener { bytes: ByteArray? ->
                                    val publicKeyRef = storageRef.child(
                                        "Public_Keys/" + tempUser?.username.toString() + ".key"
                                    )
                                    publicKeyRef.getBytes(Long.MAX_VALUE)
                                        .addOnSuccessListener { bytes1: ByteArray? ->
                                            userListDBHelper?.insertUser(
                                                tempUser?.name,
                                                tempUser?.username.toString(),
                                                bytes,
                                                Base64.decode(
                                                    fb?.message,
                                                    Base64.DEFAULT
                                                ),
                                                bytes1
                                            )
                                        }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
//                myRef.child(snapshot.key.toString()).removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}