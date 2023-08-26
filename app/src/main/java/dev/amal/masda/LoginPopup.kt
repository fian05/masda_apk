package dev.amal.masda

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

@SuppressLint("SetTextI18n")
class LoginPopup(context : Context) {
    private val sharedPrefManager = SharedPrefManager(context)
    init {
        val inflater = LayoutInflater.from(context)
        if (sharedPrefManager.sPSudahInputNISN) {
            val view = inflater.inflate(R.layout.akun_layout, null)
            val builder = AlertDialog.Builder(context)
            builder.setView(view)
            //builder.setCancelable(false)
            // Init
            val tvNama : TextView = view.findViewById(R.id.tvNama)
            val tvNISN : TextView = view.findViewById(R.id.tvNISN)
            val tvSekolah : TextView = view.findViewById(R.id.tvSekolah)
            val btnClose : ImageButton = view.findViewById(R.id.btnClose)
            val btnUbahPassword : Button = view.findViewById(R.id.btnUbahPassword)
            val btnLogoutNISN : Button = view.findViewById(R.id.btnLogoutNISN)
            val pref2 = context.getSharedPreferences("akun", AppCompatActivity.MODE_PRIVATE)
            tvNama.text = " "+pref2.getString("nama", null)
            tvNISN.text = " "+pref2.getString("nisn", null)
            tvSekolah.text = " "+pref2.getString("sekolah", null)
            val alertDialog = builder.create()
            alertDialog.show()
            btnClose.setOnClickListener {
                alertDialog.dismiss()
            }
            btnUbahPassword.setOnClickListener {
                alertDialog.dismiss()
                val view2 = inflater.inflate(R.layout.ubah_password_layout, null)
                val builder2 = AlertDialog.Builder(context)
                builder2.setView(view2)
                builder2.setCancelable(false)
                // Inisialisasi komponen input password lama dan baru
                val etPasswordLama : TextInputEditText = view2.findViewById(R.id.pw_old_text_input_edit_text)
                val etPasswordBaru : TextInputEditText = view2.findViewById(R.id.pw_new_text_input_edit_text)
                val btnClose2 : ImageButton = view2.findViewById(R.id.btnClose)
                val btnSubmitUbahPassword : Button = view2.findViewById(R.id.btnSubmitUbahPassword)
                val alertDialog2 = builder2.create()
                alertDialog2.show()
                btnClose2.setOnClickListener {
                    alertDialog2.dismiss()
                    LoginPopup(context)
                }
                btnSubmitUbahPassword.setOnClickListener {
                    val pwOld = etPasswordLama.text.toString()
                    val pwNew = etPasswordBaru.text.toString()
                    if (pwOld.isEmpty() || pwNew.isEmpty()) {
                        Toast.makeText(context, "Inputan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    } else if (pwOld.length < 8 || pwOld.length > 16) {
                        Toast.makeText(context, "Password minimal 8 karakter dan maksimal 16 karakter!", Toast.LENGTH_SHORT).show()
                    } else if (pwNew.length < 8 || pwNew.length > 16) {
                        Toast.makeText(context, "Password minimal 8 karakter dan maksimal 16 karakter!", Toast.LENGTH_SHORT).show()
                    } else if (pwOld == pwNew || pwNew == "12345678") {
                        Toast.makeText(context, "Password baru tidak boleh sama dengan yang lama", Toast.LENGTH_SHORT).show()
                    } else {
                        val jsonObject = JSONObject()
                        val requestQueue = Volley.newRequestQueue(context)
                        jsonObject.put("nisn", pref2.getString("nisn", null))
                        jsonObject.put("password_old", pwOld)
                        jsonObject.put("password_new", pwNew)
                        val jsonObjectRequest = JsonObjectRequest(
                            Request.Method.POST,
                            API.gantiPassword,
                            jsonObject,
                            { response ->
                                if (response.getString("success").contains("true", ignoreCase = true)) {
                                    Toast.makeText(context, "Password Anda berhasil diubah!", Toast.LENGTH_SHORT).show()
                                    etPasswordLama.setText("")
                                    etPasswordBaru.setText("")
                                    alertDialog2.dismiss()
                                    (context as? MainActivity)?.refreshMainActivity()
                                } else {
                                    Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show()
                                }
                            },
                            {
                                // Toast.makeText(context, "Volley Error $error", Toast.LENGTH_SHORT).show()
                                Toast.makeText(context, "Ubah Password Gagal!", Toast.LENGTH_SHORT).show()
                            })
                        requestQueue.add(jsonObjectRequest)
                    }
                }
            }
            btnLogoutNISN.setOnClickListener {
                sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_SUDAH_INPUT_NISN, false)
                val pref3 = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = pref3.edit()
                editor.remove("nama")
                editor.remove("nisn")
                editor.remove("sekolah")
                editor.apply()
                alertDialog.dismiss()
                (context as? MainActivity)?.refreshMainActivity()
            }
        } else {
            val view = inflater.inflate(R.layout.login_layout, null)
            val builder = AlertDialog.Builder(context)
            builder.setView(view)
            builder.setCancelable(false)
            // Init
            val editNisn : TextInputEditText = view.findViewById(R.id.nisn_text_input_edit_text)
            val editPassword : TextInputEditText = view.findViewById(R.id.password_text_input_edit_text)
            val btnClose : ImageButton = view.findViewById(R.id.btnClose)
            val btnSubmitNISN : Button = view.findViewById(R.id.btnSubmitNISN)
            val alertDialog = builder.create()
            alertDialog.show()
            btnClose.setOnClickListener {
                alertDialog.dismiss()
            }
            btnSubmitNISN.setOnClickListener {
                val nisn = editNisn.text.toString()
                val password = editPassword.text.toString()
                if (nisn.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "NISN / Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                } else if (password.length < 8) {
                    Toast.makeText(context, "Password salah!", Toast.LENGTH_SHORT).show()
                } else {
                    val jsonObject = JSONObject()
                    val requestQueue = Volley.newRequestQueue(context)
                    jsonObject.put("nisn", nisn)
                    jsonObject.put("password", password)
                    val jsonObjectRequest = JsonObjectRequest(
                        Request.Method.POST,
                        API.login,
                        jsonObject,
                        { response ->
                            if (response.getString("success").contains("true", ignoreCase = true)) {
                                Toast.makeText(context, "Selamat Datang, " + response.getString("message"), Toast.LENGTH_SHORT).show()
                                editNisn.setText("")
                                editPassword.setText("")
                                sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_SUDAH_INPUT_NISN, true)
                                val pref = context.getSharedPreferences("akun", AppCompatActivity.MODE_PRIVATE)
                                val editor = pref.edit()
                                editor.putString("nama", response.getString("message"))
                                editor.putString("nisn", nisn)
                                editor.putString("sekolah", response.getString("sekolah"))
                                editor.apply()
                                alertDialog.dismiss()
                                (context as? MainActivity)?.refreshMainActivity()
                            } else {
                                Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                        },
                        {
                            // Toast.makeText(this, "Volley Error $error", Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, "Login Gagal", Toast.LENGTH_SHORT).show()
                        })
                    requestQueue.add(jsonObjectRequest)
                }
            }
        }
    }
}