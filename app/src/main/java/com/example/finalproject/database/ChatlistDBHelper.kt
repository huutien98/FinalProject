package com.example.finalproject.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.Timestamp

class ChatlistDBHelper
    (context: Context?) : SQLiteOpenHelper(context, CHATS_DATABASE_NAME, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun addUser(userTableName: String) {
        val sql =
            "create table if not exists $userTableName (_id integer primary key autoincrement, $CHATS_IS_USER integer,$CHATS_MESSAGE blob,$CHATS_MESSAGE_TIME timestamp)"
        this.writableDatabase.execSQL(sql)
    }

    fun insertMessage(tableName: String?, message: ByteArray?, isUser: Boolean): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        if (isUser) contentValues.put(CHATS_IS_USER, IS_USER) else contentValues.put(
            CHATS_IS_USER,
            IS_OTHER
        )
        contentValues.put(CHATS_MESSAGE, message)
        val timestamp = Timestamp(System.currentTimeMillis())
        contentValues.put(CHATS_MESSAGE_TIME, timestamp.toString())
        val p = db.insert(tableName, null, contentValues)
        return p != -1L
    }

    fun deleteMsg(tableName: String, msg_id: Int): Boolean {
        val db = this.writableDatabase
        val cursor =
            db.rawQuery("Select * from $tableName where _id =?", arrayOf(msg_id.toString()))
        val p = db.delete(tableName, "_id =?", arrayOf(msg_id.toString())).toLong()
        return p != -1L
    }

    fun getAllMessages(tableName: String): Cursor {
        val db = this.writableDatabase
        return db.rawQuery("Select * from $tableName", null)
    }

    fun getNewMessages(
        tableName: String,
        last_msg_id: Int
    ): Cursor {
        val db = this.writableDatabase
        return db.rawQuery(
            "Select * from $tableName where _id >?",
            arrayOf(last_msg_id.toString())
        )
    }

    companion object {
        const val CHATS_DATABASE_NAME = "chatlist.db"
        const val CHATS_IS_USER = "isUser"
        const val CHATS_MESSAGE = "message"
        const val CHATS_MESSAGE_TIME = "messageTime"
        const val IS_USER = 1
        const val IS_OTHER = 0
        const val VERSION = 1
    }
}