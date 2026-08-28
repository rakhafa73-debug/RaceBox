package com.racebox.app.data.prefs

import org.json.JSONObject

data class UserSession(
    val userId: Long,
    val username: String
) {
    fun toJson(): String =
        JSONObject()
            .put(KEY_USER_ID, userId)
            .put(KEY_USERNAME, username)
            .toString()

    companion object {
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"

        fun fromJson(json: String): UserSession {
            val obj = JSONObject(json)
            return UserSession(
                userId = obj.getLong(KEY_USER_ID),
                username = obj.getString(KEY_USERNAME)
            )
        }
    }
}