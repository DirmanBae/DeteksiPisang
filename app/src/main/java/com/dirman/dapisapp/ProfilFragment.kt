package com.dirman.dapisapp

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfilFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfilFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("users").child(uid!!)

        val txtProfileName = view.findViewById<TextView>(R.id.txtProfileName)
        val txtProfileEmail = view.findViewById<TextView>(R.id.txtProfileEmail)
        val txtProfileLevel = view.findViewById<TextView>(R.id.txtProfileLevel)
        val btnEditProfil = view.findViewById<View>(R.id.btnEditProfil)

        // Tampilkan data
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("nama").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val level = snapshot.child("level").getValue(String::class.java)

                txtProfileName.text = name ?: "Unknown"
                txtProfileEmail.text = email ?: "Unknown"
                txtProfileLevel.text = level ?: "Unknown"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal muat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Fungsi Edit Profil
        btnEditProfil.setOnClickListener {
            val currentName = txtProfileName.text.toString()
            val currentEmail = txtProfileEmail.text.toString()

            val inputView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
            val etNama = inputView.findViewById<TextView>(R.id.etNama)
            val etEmail = inputView.findViewById<TextView>(R.id.etEmail)

            etNama.text = currentName
            etEmail.text = currentEmail

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Profil")
                .setView(inputView)
                .setPositiveButton("Simpan") { _, _ ->
                    val newName = etNama.text.toString().trim()
                    val newEmail = etEmail.text.toString().trim()

                    if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                        val updates = mapOf(
                            "nama" to newName,
                            "email" to newEmail
                        )
                        ref.updateChildren(updates).addOnSuccessListener {
                            txtProfileName.text = newName
                            txtProfileEmail.text = newEmail
                            Toast.makeText(requireContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Nama dan email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profil, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfilFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfilFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}