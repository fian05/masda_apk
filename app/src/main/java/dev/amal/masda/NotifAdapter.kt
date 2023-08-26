package dev.amal.masda

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class NotifAdapter(private val dataList : List<String>, private val dataLokasi: JSONArray, private val context: Context) : RecyclerView.Adapter<NotifAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notif_item, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        val notification = dataList[position]
        holder.bind(notification, dataLokasi)
        if (position % 2 == 0) { // Mengatur latar belakang item
            holder.itemView.setBackgroundResource(R.drawable.notif_item_background_genap)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.notif_item_background_ganjil)
        }
        holder.itemView.setOnClickListener {} // Menambahkan efek hover ketika item diklik
    }
    override fun getItemCount() : Int {
        return dataList.size
    }
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bind(notification : String, dataLokasi : JSONArray) {
            val teksNotif : TextView = itemView.findViewById(R.id.teksNotif)
            val btnLihatLokasi : Button = itemView.findViewById(R.id.btnLihatLokasi)
            teksNotif.text = notification
            btnLihatLokasi.setOnClickListener {
                val notificationIndex = adapterPosition
                if (notificationIndex != RecyclerView.NO_POSITION) {
                    val notif = dataLokasi.getJSONObject(notificationIndex)
                    val latitude = notif.getString("latitude")
                    val longitude = notif.getString("longitude")
                    // Arahkan ke aplikasi Google Maps menggunakan koordinat latitude dan longitude
                    val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
            }
        }
    }
}
