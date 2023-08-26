package dev.amal.masda

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        // Status Bar melayang
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        // Get App Version
        val appVersion = BuildConfig.VERSION_NAME
        val textVersion : TextView = findViewById(R.id.text_version)
        textVersion.text = getString(R.string.app_version, appVersion)
        // Pengecekan koneksi internet
        val isInternetConnected = isInternetConnected()
        if (isInternetConnected) {
            showProgressBar()
            startMainActivityWithDelay()
        } else {
            showProgressBar()
            Handler(Looper.getMainLooper()).postDelayed({
                showNoInternetDialog()
            }, 3250) // Menunggu 3,25 detik sebelum menampilkan AlertDialog
        }
    }
    private fun startMainActivityWithDelay() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(4000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
    private fun showProgressBar() {
        val loadingSplash : ProgressBar = findViewById(R.id.loadingSplash)
        loadingSplash.visibility = View.VISIBLE
    }
    private fun showNoInternetDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Mohon periksa koneksi Anda!")
        dialogBuilder.setPositiveButton("Refresh") { _, _ ->
            recreate() // Refresh ulang activity
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
        // Mengubah warna tombol Refresh menjadi putih
        val positiveButton : Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.teal_200))
    }
    private fun isInternetConnected() : Boolean {
        // Mendapatkan instance dari ConnectivityManager
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Mengecek apakah koneksi WiFi atau koneksi data seluler tersedia
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}