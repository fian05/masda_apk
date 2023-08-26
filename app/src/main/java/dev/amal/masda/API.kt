package dev.amal.masda

class API {
    companion object {
        private const val SERVER = "http://masda.fian.biz.id/"
        const val maps = SERVER+"monitoring/maps" // untuk yang belum login
        const val mapsPelajar = SERVER+"monitoring/pelajar/" // butuh tambahan NISN di API nya
        const val login = SERVER+"api/monitoring/loginNISN"
        const val gantiPassword = SERVER+"api/monitoring/pelajar/updatePassword"
        const val getNotif = SERVER+"api/monitoring/pelajar/getNotifikasi"
        const val listNotif = SERVER+"api/monitoring/pelajar/listNotifikasi"
    }
}