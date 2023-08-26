package dev.amal.masda

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context : Context) {
    companion object {
        const val SP_BELUM_INPUT_NISN = "spBelumInputNISN"
        const val SP_SUDAH_INPUT_NISN = "spSudahInputNISN"
    }
    private var sp : SharedPreferences
    private var spEditor : SharedPreferences.Editor
    init {
        sp = context.getSharedPreferences(SP_BELUM_INPUT_NISN, Context.MODE_PRIVATE)
        spEditor = sp.edit()
    }
    fun saveSPBoolean(keySP : String?, value : Boolean) {
        spEditor.putBoolean(keySP, value)
        spEditor.commit()
    }
    val sPSudahInputNISN : Boolean
        get() = sp.getBoolean(SP_SUDAH_INPUT_NISN, false)
}