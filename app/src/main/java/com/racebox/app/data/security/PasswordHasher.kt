package com.racebox.app.data.security

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {

    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(COST))

    fun verify(password: String, hash: String): Boolean =
        try {
            BCrypt.checkpw(password, hash)
        } catch (_: IllegalArgumentException) {
            false
        }

    private const val COST = 12
}