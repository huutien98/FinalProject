package com.example.finalproject.model

class User {
    var name: String? = null
    var username: String? = null
    var email_id: String? = null

    constructor() {}
    constructor(name: String?, username: String?, email_id: String?) {
        this.name = name
        this.username = username
        this.email_id = email_id
    }
}