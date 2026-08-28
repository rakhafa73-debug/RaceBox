package com.racebox.app.repository

import com.racebox.app.data.db.dao.UserDao
import com.racebox.app.data.prefs.SecurePrefs
import com.racebox.app.data.prefs.UserSession
import com.racebox.app.data.security.PasswordHasher
import com.racebox.app.data.db.entity.User

class AuthRepository(
    private val userDao: UserDao,
    private val securePrefs: SecurePrefs
) {

    suspend fun login(username: String, password: String): AuthResult {
        val name = username.trim()
        if (name.isEmpty() || password.isEmpty()) {
            return AuthResult.Error("Nama pengguna dan kata sandi wajib diisi")
        }
        val user = userDao.findByUsername(name)
        return if (user == null) {
            val id = userDao.insert(
                User(
                    username = name,
                    passwordHash = PasswordHasher.hash(password)
                )
            )
            val created = User(id = id, username = name, passwordHash = "")
            securePrefs.saveSession(UserSession(created.id, created.username))
            AuthResult.Success(created.id, created.username)
        } else {
            if (PasswordHasher.verify(password, user.passwordHash)) {
                securePrefs.saveSession(UserSession(user.id, user.username))
                AuthResult.Success(user.id, user.username)
            } else {
                AuthResult.Error("Kata sandi salah")
            }
        }
    }

    fun currentUser(): UserSession? = securePrefs.userSession()

    fun logout() {
        securePrefs.clear()
    }
}

sealed interface AuthResult {
    data class Success(val userId: Long, val username: String) : AuthResult
    data class Error(val message: String) : AuthResult
}