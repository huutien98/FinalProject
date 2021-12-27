package com.example.finalproject.model

data class UserListComponent(
    var chatName: String,
    var chatUsername: String,
    var lastMessage: String,
    var profilePic: ByteArray,
    var lastTextTime: String
)