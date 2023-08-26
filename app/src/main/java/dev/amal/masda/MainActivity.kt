package dev.amal.masda

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var doubleBackToExitPressedOnce = false // Keperluan Double Click to Exit
    private lateinit var refresh : ImageView
    private lateinit var notif : ImageView
    private lateinit var login : ImageView
    private lateinit var loading : ProgressBar
    private lateinit var webView : WebView
    private lateinit var urlWebView : String // Keperluan url WebView
    private lateinit var sharedPrefManager : SharedPrefManager
    private var permissionDeniedCount = 0
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) { // Izin ditolak
            permissionDeniedCount++
            if (permissionDeniedCount >= 2) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent)
            } else {
                showPermissionPopup()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        setupUI()
        refresh.setOnClickListener { refreshWebView() }
        if (sharedPrefManager.sPSudahInputNISN) {
            val pref5 = applicationContext.getSharedPreferences("akun", MODE_PRIVATE)
            notif.visibility = View.VISIBLE
            notif.setOnClickListener {
                NotifPopup(this, pref5.getString("nisn", null), pref5.getString("nama", null))
            }
        } else {
            notif.visibility = View.GONE
        }
        login.setOnClickListener { LoginPopup(this) }
        // ACTION BAR & STATUS BAR
        actionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue_app)
    }
    private fun init() {
        refresh = findViewById(R.id.refresh)
        notif = findViewById(R.id.notif)
        login = findViewById(R.id.login)
        sharedPrefManager = SharedPrefManager(this)
        if (sharedPrefManager.sPSudahInputNISN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Versi Android 13 (Tiramisu) atau lebih tinggi
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            val pref4 = applicationContext.getSharedPreferences("akun", MODE_PRIVATE)
            urlWebView = API.mapsPelajar+pref4.getString("nisn", null)
            // Kirim ke BootCompletedReceiver
            val intent = Intent(this, BootCompletedReceiver::class.java)
            intent.putExtra("nisn", pref4.getString("nisn", null))
            intent.putExtra("nama", pref4.getString("nama", null))
            // WorkManager UNTUK KEPERLUAN NOTIFIKASI
            val data = Data.Builder()
                .putString("nisn", pref4.getString("nisn", null))
                .putString("nama", pref4.getString("nama", null))
                .build()
            val workRequest = OneTimeWorkRequestBuilder<NotifWorker>()
                .setInputData(data)
                .setInitialDelay(8, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(this).enqueueUniqueWork(
                "notif_work",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } else {
            urlWebView = API.maps
            WorkManager.getInstance(this).cancelAllWork() // Matikan service WorkManager
        }
        loading = findViewById(R.id.pbLoading)
        webView = findViewById(R.id.wvMaps)
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionPopup() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Izin notifikasi diperlukan untuk keperluan monitoring pelajar")
        dialogBuilder.setPositiveButton("OK") { _, _ ->
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
        val positiveButton : Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.teal_200))
    }
    fun shortenName(fullName: String): String {
        val nameParts = fullName.split(" ").toTypedArray()
        val firstName = if (nameParts.size >= 2) {
            "${nameParts[0]} ${nameParts[1]}"
        } else {
            fullName
        }
        var lastInitials = ""
        for (i in 2 until nameParts.size) {
            lastInitials += "${nameParts[i][0]}. "
        }
        lastInitials = lastInitials.trimEnd()
        return "$firstName $lastInitials"
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //stopService(Intent(this, MainActivity::class.java))
            //WorkManager.getInstance(this).cancelAllWork() // Matikan service WorkManager
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Tekan BACK lagi untuk keluar aplikasi", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 1000)
    }
    override fun onKeyDown(keyCode : Int, event : KeyEvent) : Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    inner class MyWebclient : WebViewClient() {
        override fun onPageFinished(view : WebView, url : String) {
            super.onPageFinished(view, url)
            loading.visibility = View.GONE
        }
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view : WebView, url : String) : Boolean {
            if (url.startsWith("https://maps.google.com/maps?q=")) {
                val originalUrl = url.substringAfter("https://maps.google.com/maps?q=")
                val geoUrl = url.replace("https://maps.google.com/maps?q=", "geo:")
                val finalUrl = "$geoUrl?q=$originalUrl"
                val coordinates = extractCoordinatesFromUrl(finalUrl)
                if (coordinates != null) {
                    val gmmIntentUri = Uri.parse(finalUrl)
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
                return true
            }
            view.loadUrl(url)
            return true
        }
        private fun extractCoordinatesFromUrl(url : String) : String? {
            val uri = Uri.parse(url)
            if (uri.scheme == "geo" && uri.schemeSpecificPart != null) {
                val coordinates = uri.schemeSpecificPart.split(",")
                if (coordinates.size >= 2) {
                    val latitude = coordinates[0]
                    val longitude = coordinates[1]
                    return "$latitude,$longitude"
                }
            }
            return null
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebview(){
        webView.webViewClient = MyWebclient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(urlWebView)
    }
    private fun isOnline(context : Context) : Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }
    private fun setupUI(){
        if (isOnline(this)) {
            loadWebview()
        } else {
            // Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                showNoInternetDialog()
            }, 3250) // Menunggu 3,25 detik sebelum menampilkan AlertDialog
        }
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
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.teal_200))
    }
    private fun refreshWebView() {
        if (isOnline(this)) {
            webView.reload() // Refresh WebView
        } else {
            // Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                showNoInternetDialog()
            }, 3250) // Menunggu 3,25 detik sebelum menampilkan AlertDialog
        }
    }
    fun refreshMainActivity() {
        // Membuat Intent untuk memperbarui MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}