package com.racebox.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.racebox.app.data.sync.SyncWorker

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestNotificationPermission()
        SyncWorker.schedule(this)

        if (savedInstanceState == null) {
            val container = (application as RaceBoxApp).container
            if (container.authRepository.currentUser() != null) {
                val navController = findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.action_login_to_dashboard)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_NOTIFICATIONS)
        }
    }

    private companion object {
        const val PERMISSION_NOTIFICATIONS = 1001
    }
}