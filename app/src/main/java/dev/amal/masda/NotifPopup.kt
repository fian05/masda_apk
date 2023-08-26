package dev.amal.masda

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class NotifPopup(context : Context, nisn : String?, nama : String?) {
    init {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API.listNotif,
            JSONObject().put("nisn", nisn),
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val data = response.getJSONArray("data")
                        // alertDialog
                        val inflater = LayoutInflater.from(context)
                        val view = inflater.inflate(R.layout.notif_layout, null)
                        val builder = AlertDialog.Builder(context)
                        builder.setView(view)
                        builder.setCancelable(true)
                        val alertDialog = builder.create()
                        alertDialog.show()
                        val closeButton : ImageButton = view.findViewById(R.id.btnClose)
                        closeButton.setOnClickListener {
                            alertDialog.dismiss()
                        }
                        // recyclerView notifikasi
                        val recyclerView : RecyclerView = view.findViewById(R.id.recyclerView)
                        recyclerView.layoutManager = LinearLayoutManager(context)
                        val dataList = ArrayList<String>()
                        for (i in 0 until data.length()) {
                            val notification = data.getJSONObject(i)
                            val platNomor =
                                notification.getString("plat_nomor").replace(Regex("(\\d+)"), " $1 ")
                            var status = notification.getString("status")
                            val createdAt = notification.getString("created_at")
                            // Ubah format tanggal
                            val dateTime =
                                ZonedDateTime.parse(createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            val zoneId = ZoneId.of("Asia/Jakarta")
                            val formatter =
                                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'pukul' HH:mm 'WIB'")
                                    .withLocale(Locale("id", "ID"))
                                    .withZone(zoneId)
                            val formattedDateTime = dateTime.format(formatter)
                            // Cek status
                            var teks : String? = null
                            if (status == "in") {
                                status = "sedang menaiki bus"
                                teks =
                                    nama?.let { MainActivity().shortenName(it) } + " saat ini $status sekolah $platNomor pada $formattedDateTime."
                            } else if (status == "out") {
                                status = "sudah turun dari bus"
                                teks =
                                    nama?.let { MainActivity().shortenName(it) } + " saat ini $status sekolah $platNomor pada $formattedDateTime."
                            }
                            teks?.let { dataList.add(it) }
                        }
                        // adapter recyclerView
                        val adapter = NotifAdapter(dataList, data, context)
                        recyclerView.adapter = adapter
                    } else { // Tidak ada notifikasi
                        // Toast.makeText(applicationContext, "tidak ada data $nisn", Toast.LENGTH_SHORT).show()
                    }
                } catch (e : JSONException) { // Tangani kesalahan parsing JSON
                    // Toast.makeText(applicationContext, "Error: $e", Toast.LENGTH_SHORT).show()
                }
            },
            {   // Tangani kesalahan yang terjadi saat meminta notifikasi
            }
        )
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(jsonObjectRequest)
    }
}