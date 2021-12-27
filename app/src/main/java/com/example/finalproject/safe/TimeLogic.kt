package com.example.finalproject.safe

import java.sql.Timestamp

object TimeLogic {
    fun CustomTimeFormat(dateTime: String): String {
        return try {
            val currentTime = Timestamp(System.currentTimeMillis()).toString()
            if (getYear(currentTime) != getYear(dateTime)) {
                return "Last year"
            }
            if (getDate(currentTime) != getDate(dateTime)) {
                getDate(dateTime)
            } else getTime(dateTime)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getYear(dateTime: String): String {
        return dateTime.substring(0, 4)
    }

    private fun getDate(dateTime: String): String {
        return dateTime.substring(5, 10)
    }

    private fun getTime(dateTime: String): String {
        return dateTime.substring(11, 16)
    }
}