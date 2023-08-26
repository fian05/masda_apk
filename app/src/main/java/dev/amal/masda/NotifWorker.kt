package dev.amal.masda

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class NotifWorker(context : Context, workerParams : WorkerParameters) : Worker(context, workerParams) {
    override fun doWork() : Result {
        val nisn = inputData.getString("nisn")
        val nama = inputData.getString("nama")
        sendNotification(applicationContext, nisn, nama)
        scheduleNextWork(nisn, nama)
        return Result.success()
    }
    @SuppressLint("ServiceCast", "NotificationPermission")
    private fun sendNotification(context : Context, nisn : String?, nama : String?) {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API.getNotif,
            JSONObject().put("nisn", nisn),
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val platNomor = response.getString("plat_nomor").replace(Regex("(\\d+)"), " $1 ")
                        var status = response.getString("status")
                        val latitude = response.getString("latitude")
                        val longitude = response.getString("longitude")
                        val createdAt = response.getString("created_at")
                        // ubah format tanggal
                        val dateTime = ZonedDateTime.parse(createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        val zoneId = ZoneId.of("Asia/Jakarta")
                        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'pukul' HH:mm 'WIB'")
                            .withLocale(Locale("id", "ID"))
                            .withZone(zoneId)
                        val formattedDateTime = dateTime.format(formatter)
                        // cek status
                        var teks : String? = null
                        if(status == "in") {
                            status = "sedang menaiki bus"
                            teks = nama?.let { MainActivity().shortenName(it) } +" saat ini $status sekolah $platNomor pada $formattedDateTime."
                        } else if(status == "out") {
                            status = "sudah turun dari bus"
                            teks = nama?.let { MainActivity().shortenName(it) } +" saat ini $status sekolah $platNomor pada $formattedDateTime."
                        }
                        // NOTIFIKASI
                        val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val notifyID = 1002
                        val name = "MASBIS"
                        val id = "masbis_app"
                        val description = "masbis_monitoring_angkutan_sekolah_bis"
                        val pendingIntent : PendingIntent
                        val importance = NotificationManager.IMPORTANCE_HIGH
                        var mChannel = notifyManager.getNotificationChannel(id)
                        if (mChannel == null) {
                            mChannel = NotificationChannel(id, name, importance)
                            mChannel.description = description
                            mChannel.enableVibration(true)
                            mChannel.lightColor = Color.GREEN
                            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                            notifyManager.createNotificationChannel(mChannel)
                        }
                        val builder : NotificationCompat.Builder = NotificationCompat.Builder(context, id)
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                        builder.setContentTitle("Hai sobat MASDA!")
                            .setSmallIcon(R.drawable.icon)
                            .setContentText(teks)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setTicker("Notification")
                            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                        // Arahkan ke aplikasi Google Maps menggunakan koordinat latitude dan longitude
                        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
                        val viewLocationIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        val pendingViewLocationIntent = PendingIntent.getActivity(context, 0, viewLocationIntent, PendingIntent.FLAG_IMMUTABLE)
                        val lihatLokasi = NotificationCompat.Action(R.drawable.icon, "Lihat Lokasi", pendingViewLocationIntent)
                        builder.addAction(lihatLokasi)
                        val notification = builder.build()
                        notifyManager.notify(notifyID, notification)

                    } else { // Tidak ada notifikasi
                        // buat cek perubahan nama misal ada
                        val pref = context.getSharedPreferences("akun", AppCompatActivity.MODE_PRIVATE)
                        val namaBaru = response.getString("nama")
                        val editor = pref.edit()
                        editor.putString("nama", namaBaru)
                        editor.apply()
                    }
                } catch (e : JSONException) { // Tangani kesalahan parsing JSON
                    // Toast.makeText(applicationContext, "Error: $e", Toast.LENGTH_SHORT).show()
                }
            },
            {   // Tangani kesalahan yang terjadi saat meminta notifikasi
            }
        )
        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(jsonObjectRequest)
    }
    private fun scheduleNextWork(nisn : String?, nama : String?) {
        val inputData = Data.Builder()
            .putString("nisn", nisn)
            .putString("nama", nama)
            .build()
        val nextWorkRequest = OneTimeWorkRequestBuilder<NotifWorker>()
            .setInputData(inputData)
            .setInitialDelay(8, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "notif_work",
            ExistingWorkPolicy.REPLACE,
            nextWorkRequest
        )
    }
}