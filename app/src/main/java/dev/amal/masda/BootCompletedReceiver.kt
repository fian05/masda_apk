package dev.amal.masda

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val nisn = intent.getStringExtra("nisn")
            val nama = intent.getStringExtra("nama")
            if (nisn != null && nama != null) {
                // Ketika device nyala kembali
                val data = Data.Builder()
                    .putString("nisn", nisn)
                    .putString("nama", nama)
                    .build()
                val workRequest = OneTimeWorkRequestBuilder<NotifWorker>()
                    .setInputData(data)
                    .setInitialDelay(8, TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(context!!).enqueueUniqueWork(
                    "notif_work",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }
}