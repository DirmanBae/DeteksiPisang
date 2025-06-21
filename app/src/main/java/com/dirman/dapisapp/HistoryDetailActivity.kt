package com.dirman.dapisapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class HistoryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)

        val ivDetailGambar = findViewById<ImageView>(R.id.ivDetailGambar)
        val tvDetailDeteksi = findViewById<TextView>(R.id.tvDetailDeteksi)
        val tvDetailAkurasi = findViewById<TextView>(R.id.tvDetailAkurasi)
        val tvDetailTanggal = findViewById<TextView>(R.id.tvDetailTanggal)
        val tvDetailNama = findViewById<TextView>(R.id.tvDetailNama)
        val tvDetailCatatan = findViewById<TextView>(R.id.tvDetailCatatan)


        val gambar = intent.getStringExtra("gambar")
        val deteksi = intent.getStringExtra("hasil_deteksi")
        val akurasi = intent.getStringExtra("akurasi")
        val tanggal = intent.getStringExtra("tgl")
        val nama = intent.getStringExtra("nama")
        val catatan = intent.getStringExtra("catatan_penyuluh") ?: "Tidak ada catatan"

        Glide.with(this).load(gambar).into(ivDetailGambar)
        tvDetailDeteksi.text = "Deteksi: $deteksi"
        tvDetailAkurasi.text = "Akurasi: $akurasi"
        tvDetailTanggal.text = "Tanggal: $tanggal"
        tvDetailNama.text = "Pengguna: $nama"
        tvDetailCatatan.text = "Catatan Penyuluh: $catatan"
    }
}
