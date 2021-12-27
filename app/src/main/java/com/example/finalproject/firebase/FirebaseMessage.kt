package com.example.finalproject.firebase

class FirebaseMessage {
    var username: String? = null
    var message: String? = null

    constructor() {}
    constructor(username: String?, message: String?) {
        this.username = username
        this.message = message
    }
}
