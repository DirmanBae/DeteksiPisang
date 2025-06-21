package com.dirman.dapisapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var historyList: MutableList<HistoryItem>
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // Inisialisasi RecyclerView
        rvHistory = view.findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())

        // Inisialisasi list dan adapter
        historyList = mutableListOf()
        adapter = HistoryAdapter(historyList) { idHistory ->
            // Callback saat item dihapus, hapus dari list dan perbarui adapter
            val index = historyList.indexOfFirst { it.id_history == idHistory }
            if (index != -1) {
                historyList.removeAt(index)
                adapter.notifyItemRemoved(index)
            }
        }
        rvHistory.adapter = adapter

        // Cek autentikasi
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show()
            return view
        }

        // Ambil data history dari Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("history")
        databaseRef.orderByChild("id_user").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    historyList.clear()
                    for (data in snapshot.children) {
                        val item = data.getValue(HistoryItem::class.java)
                        item?.let {
                            it.id_history = data.key
                            historyList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            })

        return view
    }
}
