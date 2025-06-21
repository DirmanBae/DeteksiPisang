package com.dirman.dapisapp

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Locale
import android.Manifest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var btnSelectImage: Button
    private lateinit var ivSelectedImage: ImageView
    private var imageUri: Uri? = null
    private lateinit var btnDetect: Button // Deklarasi Button Deteksi
    private lateinit var tvDetectionResult: TextView // Deklarasi TextView Hasil Deteksi
    val labels = listOf("Daun Sehat", "Sigatoka", "Cordana", "Pestaliopsis")


    // ActivityResultLauncher untuk Gallery
    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let {
                    imageUri = it // ✅ Simpan URI-nya
                    ivSelectedImage.setImageURI(it)
                } ?: Toast.makeText(requireContext(), "Gagal memilih gambar dari galeri.", Toast.LENGTH_SHORT).show()
            }
        }


    // ActivityResultLauncher untuk Camera
    private val cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri?.let { uri ->
                    ivSelectedImage.setImageURI(uri)
                } ?: Toast.makeText(requireContext(), "Gagal mengambil gambar dari kamera.", Toast.LENGTH_SHORT).show()
            }
        }


    // ActivityResultLauncher untuk Request Permissions
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val readStorageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

            if (cameraGranted) {
                // Izin kamera diberikan, lanjutkan untuk mengambil gambar
                dispatchTakePictureIntent()
            } else if (readStorageGranted) {
                // Izin galeri diberikan, lanjutkan untuk memilih gambar
                dispatchPickPictureIntent()
            } else {
                Toast.makeText(requireContext(), "Beberapa izin penting ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
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
    private lateinit var classifier: LogicDeteksi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        ivSelectedImage = view.findViewById(R.id.ivSelectedImage)
        val btnDetect: Button = view.findViewById(R.id.btnDetect)
        val tvResult: TextView = view.findViewById(R.id.tvDetectionResult)
        classifier = LogicDeteksi(requireContext())

        btnSelectImage.setOnClickListener {
            showImageSourceDialog()
        }

        btnDetect.setOnClickListener {
            val drawable = ivSelectedImage.drawable
            if (drawable != null && drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val (penyakit, akurasi) = classifier.classify(bitmap)
                val hasilAkhir = "$penyakit\nAkurasi: ${"%.2f".format(akurasi)}%"
                tvResult.text = hasilAkhir

//                menyimpan hasil deteksi kedalam databse
                simpanHasilKeFirebase(penyakit, akurasi)
            } else {
                Toast.makeText(requireContext(), "Gambar belum dipilih!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showImageSourceDialog() {
        val options = arrayOf<CharSequence>("Kamera", "Galeri")
        AlertDialog.Builder(requireContext()) // Gunakan requireContext()
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission() // Kamera
                    1 -> checkGalleryPermission() // Galeri
                }
            }
            .show()
    }

    private fun simpanHasilKeFirebase(hasilDeteksi: String, akurasi: Float) {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.get().addOnSuccessListener { snapshot ->
            val namaUser = snapshot.child("nama").getValue(String::class.java) ?: "Pengguna"
            val tanggal = SimpleDateFormat("dd-MM-yyyy / HH:mm:ss", Locale.getDefault()).format(Date())
            val idHistory = "HTR${System.currentTimeMillis()}"

            val imageUrl = imageUri?.toString() ?: "kosong" // URI lokal (opsional)

            val data = mapOf(
                "gambar" to imageUrl,
                "hasil_deteksi" to hasilDeteksi,
                "akurasi" to "%.2f".format(akurasi),
                "id_user" to uid,
                "nama" to namaUser,
                "tgl" to tanggal,
                "catatan_penyuluh" to "tidak ada catatan"
            )

            FirebaseDatabase.getInstance().getReference("history").child(idHistory)
                .setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Hasil deteksi berhasil disimpan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show()
        }
    }






    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            // Minta izin kamera
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun checkGalleryPermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            dispatchPickPictureIntent()
        } else {
            requestPermissionLauncher.launch(arrayOf(permission))
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Gunakan requireActivity().packageManager untuk context yang benar
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(requireContext(), "Error saat membuat file gambar: ${ex.message}", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.also {
                imageUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    it // ini adalah photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                cameraLauncher.launch(takePictureIntent)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Buat nama file gambar unik
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) // Gunakan requireContext()
        val file = File.createTempFile(imageFileName, ".jpg", storageDir)
        Toast.makeText(requireContext(), "File path: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        return file
    }
    private fun dispatchPickPictureIntent() {
        val pickPictureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(pickPictureIntent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}