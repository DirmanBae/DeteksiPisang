package com.dirman.dapisapp

data class HistoryItem(
    var id_history: String? = null,
    var gambar: String? = null,
    var hasil_deteksi: String? = null,
    var akurasi: String? = null,
    var tgl: String? = null,
    var nama: String? = null,
    var catatan_penyuluh: String? = null
)
