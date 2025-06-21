package com.dirman.dapisapp

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class HistoryAdapter(
    private val list: MutableList<HistoryItem>,
    private val onItemDeleted: (String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGambar: ImageView = itemView.findViewById(R.id.ivGambar)
        val tvDeteksi: TextView = itemView.findViewById(R.id.tvDeteksi)
        val tvAkurasi: TextView = itemView.findViewById(R.id.tvAkurasi)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val btnHapus: Button = itemView.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]
        holder.tvDeteksi.text = item.hasil_deteksi
        holder.tvAkurasi.text = "Akurasi: ${item.akurasi}"
        holder.tvTanggal.text = item.tgl
        Glide.with(holder.itemView.context).load(item.gambar).into(holder.ivGambar)

        // Klik untuk detail
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, HistoryDetailActivity::class.java).apply {
                putExtra("gambar", item.gambar)
                putExtra("hasil_deteksi", item.hasil_deteksi)
                putExtra("akurasi", item.akurasi)
                putExtra("tgl", item.tgl)
                putExtra("nama", item.nama)
                putExtra("catatan_penyuluh", item.catatan_penyuluh ?: "Tidak ada catatan")
            }
            context.startActivity(intent)
        }

        // Tombol hapus
        holder.btnHapus.setOnClickListener {
            val context = holder.itemView.context
            val idHistory = item.id_history

            if (idHistory != null) {
                AlertDialog.Builder(context)
                    .setTitle("Hapus Riwayat")
                    .setMessage("Apakah Anda yakin ingin menghapus riwayat ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        FirebaseDatabase.getInstance().getReference("history")
                            .child(idHistory)
                            .removeValue()
                            .addOnSuccessListener {
                                val index = holder.adapterPosition
                                if (index != RecyclerView.NO_POSITION) {
                                    list.removeAt(index)
                                    notifyItemRemoved(index)
                                    onItemDeleted(idHistory)
                                }
                                Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                Toast.makeText(context, "ID data tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
