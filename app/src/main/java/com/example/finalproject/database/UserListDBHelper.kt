package com.example.finalproject.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.Timestamp

class UserListDBHelper

    (context: Context?) : SQLiteOpenHelper(context, USERLIST_DATABASE_NAME, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {

         val sql =
             "create table $USERLIST_TABLE_NAME (_id integer primary key autoincrement, $USERLIST_NAME text,$USERLIST_USERNAME text,$USERLIST_CHAT_TABLE_NAME text,last_message blob,last_msg_time text,profile_image blob,public_key blob)"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $USERLIST_TABLE_NAME")
        onCreate(db)
    }

    fun insertUser(
        name: String?,
        username: String,
        profile_pic: ByteArray?,
        public_key: ByteArray?
    ): Boolean {
        val cs = getUser(username)
        if (cs.count > 0) return false
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(USERLIST_NAME, name)
        contentValues.put(USERLIST_USERNAME, username)
        contentValues.put(USERLIST_LAST_MESSAGE, "")
        contentValues.put(USERLIST_LAST_MSG_TIMING, "")
        contentValues.put(USERLIST_PROFILE_PIC, profile_pic)
        contentValues.put(USERLIST_CHAT_TABLE_NAME, USERLIST_CHAT_TABLE_PREFIX + username)
        contentValues.put(USERLIST_PUBLIC_KEY, public_key)
        val p = db.insert(USERLIST_TABLE_NAME, null, contentValues)
        return p != -1L
    }

    fun insertUser(
        name: String?,
        username: String,
        profile_pic: ByteArray?,
        last_msg: ByteArray?,
        public_key: ByteArray?
    ): Boolean {
        val cs = getUser(username)
        if (cs.count > 0) return false
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(USERLIST_NAME, name)
        contentValues.put(USERLIST_USERNAME, username)
        contentValues.put(USERLIST_LAST_MESSAGE, last_msg)
        val timestamp = Timestamp(System.currentTimeMillis())
        contentValues.put(USERLIST_LAST_MSG_TIMING, timestamp.toString())
        contentValues.put(USERLIST_PROFILE_PIC, profile_pic)
        contentValues.put(USERLIST_CHAT_TABLE_NAME, USERLIST_CHAT_TABLE_PREFIX + username)
        contentValues.put(USERLIST_PUBLIC_KEY, public_key)
        val p = db.insert(USERLIST_TABLE_NAME, null, contentValues)
        return p != -1L
    }

    fun deleteUser(username: String): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "Select * from $USERLIST_TABLE_NAME where $USERLIST_USERNAME =?",
            arrayOf(username)
        )
        val p =
            db.delete(USERLIST_TABLE_NAME, "$USERLIST_USERNAME =?", arrayOf(username)).toLong()
        return p != -1L
    }

    val allUsers: Cursor
        get() {
            val db = this.writableDatabase
            return db.rawQuery(
                "Select * from $USERLIST_TABLE_NAME ORDER BY $USERLIST_LAST_MSG_TIMING DESC",
                null
            )
        }

    fun getUser(username: String): Cursor {
        val db = this.writableDatabase
        return db.rawQuery(
            "Select * from $USERLIST_TABLE_NAME where $USERLIST_USERNAME =?",
            arrayOf(username)
        )
    }

    fun updateLastText(username: String, last_message: ByteArray?): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "Select * from $USERLIST_TABLE_NAME where $USERLIST_USERNAME =?",
            arrayOf(username)
        )
        val contentValues = ContentValues()
        if (cursor.count == 0) return false
        cursor.moveToFirst()
        contentValues.put(USERLIST_NAME, cursor.getString(1))
        contentValues.put(USERLIST_USERNAME, username)
        contentValues.put(USERLIST_CHAT_TABLE_NAME, USERLIST_CHAT_TABLE_PREFIX + username)
        contentValues.put(USERLIST_LAST_MESSAGE, last_message)
        val timestamp = Timestamp(System.currentTimeMillis())
        contentValues.put(USERLIST_LAST_MSG_TIMING, timestamp.toString())
        contentValues.put(USERLIST_PROFILE_PIC, cursor.getBlob(6))
        contentValues.put(USERLIST_PUBLIC_KEY, cursor.getBlob(7))
        contentValues.put(USERLIST_CHAT_TABLE_NAME, USERLIST_CHAT_TABLE_PREFIX + username)
        val p = db.update(
            USERLIST_TABLE_NAME,
            contentValues,
            "$USERLIST_USERNAME =?",
            arrayOf(username)
        ).toLong()
        return p != -1L
    }

    companion object {
        const val USERLIST_DATABASE_NAME = "userlist.db"
        const val USERLIST_TABLE_NAME = "userlist_table"
        const val USERLIST_USERNAME = "username"
        const val USERLIST_NAME = "name"
        const val USERLIST_PUBLIC_KEY = "public_key"
        const val USERLIST_CHAT_TABLE_NAME = "chat_table_name"
        const val USERLIST_CHAT_TABLE_PREFIX = "Fleet_"
        const val USERLIST_LAST_MESSAGE = "last_message"
        const val USERLIST_LAST_MSG_TIMING = "last_msg_time"
        const val USERLIST_PROFILE_PIC = "profile_image"
        const val VERSION = 1
    }
}