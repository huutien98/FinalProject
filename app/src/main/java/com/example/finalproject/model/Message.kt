package com.example.finalproject.model

data class Message(
    var msgId: Int,
    var isUser: Boolean,
    var messageContent: String,
    var messageTime: String
)