package com.example.finalproject.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R
import com.example.finalproject.safe.RSAEncyption
import com.example.finalproject.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey

class Register2 : AppCompatActivity() {
    var primaryScreen: LinearLayout? = null
    var secondaryOverlay: LinearLayout? = null
    var tap_to_change: TextView? = null
    var imageChooser: ImageView? = null
    var name: String? = null
    var username: String? = null
    var email_id: String? = null
    var register_btn: ImageButton? = null
    var user_privateKey: PrivateKey? = null
    var user_publicKey: PublicKey? = null
    @RequiresApi(Build.VERSION_CODES.M)
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent: Intent = intent
        name = intent.getStringExtra("name")
        username = intent.getStringExtra("username")
        email_id = intent.getStringExtra("email_id")
        setContentView(R.layout.activity_register2)
        primaryScreen = findViewById<LinearLayout>(R.id.default_layout)
        secondaryOverlay = findViewById<LinearLayout>(R.id.waitingOverlay)
        secondaryOverlay?.visibility = View.INVISIBLE
        tap_to_change = findViewById<TextView>(R.id.rand)
        imageChooser = findViewById<ImageView>(R.id.profileImage)
        imageChooser?.minimumHeight = imageChooser!!.width
        tap_to_change?.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View): Boolean {
                imageChooser?.setImageResource(R.drawable.default_profile_image)
                return false
            }
        })
        imageChooser?.setImageResource(R.drawable.default_profile_image)
        imageChooser?.setOnClickListener { v: View? ->
            if (Build.VERSION.SDK_INT > +Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                }
            }
            pickImageFromGallery()
        }
        register_btn = findViewById<ImageButton>(R.id.Continue_btn)
        register_btn?.setOnClickListener(View.OnClickListener { v: View? ->
            primaryScreen?.setAlpha(0.2f)
            secondaryOverlay?.setVisibility(View.VISIBLE)


             try {
                val kp: KeyPair = RSAEncyption.generateKeyPair()
                user_privateKey = kp.private
                user_publicKey = kp.public
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            val keySharedPrefs: SharedPreferences =
                getSharedPreferences("Personal_keys", Context.MODE_PRIVATE)
            val editor1: SharedPreferences.Editor = keySharedPrefs.edit()
            val encodedPrivateKey = Base64.encodeToString(user_privateKey?.encoded, Base64.DEFAULT)
            val encodedPublicKey = Base64.encodeToString(user_publicKey?.encoded, Base64.DEFAULT)
            editor1.putString("privateKey", encodedPrivateKey)
            editor1.putString("publicKey", encodedPublicKey)
            editor1.apply()

            val baos = ByteArrayOutputStream()
            val bitmap: Bitmap = (imageChooser?.drawable as BitmapDrawable).getBitmap()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val storage: FirebaseStorage = FirebaseStorage.getInstance()
            val storageRef: StorageReference = storage.getReference()
            val imagesRef: StorageReference = storageRef.child("images/$username.jpg")
            val publicKeyRef: StorageReference = storageRef.child("Public_Keys/$username.key")
            val uploadPublicKey: UploadTask? = user_publicKey?.encoded?.let {
                publicKeyRef.putBytes(
                    it
                )
            }
            uploadPublicKey?.addOnCompleteListener(object :
                OnCompleteListener<UploadTask.TaskSnapshot?> {

                override fun onComplete(task: Task<UploadTask.TaskSnapshot?>) {
                    val uploadTask: UploadTask = imagesRef.putBytes(data)
                    run {
                        uploadTask.addOnFailureListener(object : OnFailureListener {
                            override fun onFailure(exception: Exception) {
                                Toast.makeText(
                                    this@Register2,
                                    "Oops, something's fishy!\nWanna try again?",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }).addOnSuccessListener {
                            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                            val myRef: DatabaseReference = database.getReference("Users")
                            myRef.child(username.toString())
                                .setValue(User(name, username, email_id))
                            val sharedpreferences: SharedPreferences =
                                getSharedPreferences("personal_details", Context.MODE_PRIVATE)
                            val intent = Intent(this@Register2, UserListActivity::class.java)
                            val editor: SharedPreferences.Editor = sharedpreferences.edit()
                            editor.putString("name", name)
                            editor.putString("username", username)
                            editor.putString("email_id", email_id)
                            val encodedImage = Base64.encodeToString(data, Base64.DEFAULT)
                            editor.putString("image_data", encodedImage)
                            editor.commit()
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            })
        })
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            CropImage.activity(data?.data).setAspectRatio(1, 1)
                .start(this@Register2)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri: Uri = result.getUri()
                imageChooser?.setImageURI(resultUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error: Exception = result.getError()
            }
        }
    }

    private fun userNameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.split("@").toTypedArray()[0]
        } else {
            email
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }
}