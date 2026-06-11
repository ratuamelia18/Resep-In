package com.ratu.resep_in.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R

class AddRecipeFragment : Fragment() {

    private var selectedVideoUri: Uri? = null
    private lateinit var txtVideoPath: TextView
    private lateinit var btnSimpan: Button

    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedVideoUri = result.data?.data
            txtVideoPath.text = "Video terpilih: ${selectedVideoUri?.lastPathSegment}"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_recipe, container, false)

        val edtTitle = view.findViewById<EditText>(R.id.edtTitle)
        val btnPilihVideo = view.findViewById<Button>(R.id.btnPilihVideo)
        txtVideoPath = view.findViewById(R.id.txtVideoPath)
        val edtDescription = view.findViewById<EditText>(R.id.edtDescription)
        btnSimpan = view.findViewById(R.id.btnSimpan)

        btnPilihVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "video/*"
            }
            videoPickerLauncher.launch(intent)
        }

        btnSimpan.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val desc = edtDescription.text.toString().trim()

            // 1. Validasi teks input
            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(requireContext(), "Judul dan Deskripsi wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validasi video wajib dipilih
            if (selectedVideoUri == null) {
                Toast.makeText(requireContext(), "Silakan pilih video resep terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Jalankan proses upload Cloudinary & simpan ke Firebase
            uploadVideoAndSaveToFirebase(selectedVideoUri!!, title, desc, edtTitle, edtDescription)
        }

        return view
    }

    private fun uploadVideoAndSaveToFirebase(
        videoUri: Uri,
        title: String,
        desc: String,
        edtTitle: EditText,
        edtDescription: EditText
    ) {
        btnSimpan.isEnabled = false
        btnSimpan.text = "Mengunggah Video..."
        Toast.makeText(requireContext(), "Sedang mengunggah video ke Cloudinary...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(videoUri)
            .option("resource_type", "video")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val videoUrlOnline = resultData["secure_url"].toString()

                    saveDataToFirestore(title, desc, videoUrlOnline, edtTitle, edtDescription)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(requireContext(), "Gagal upload video: ${error.description}", Toast.LENGTH_LONG).show()

                    btnSimpan.isEnabled = true
                    btnSimpan.text = "Simpan"
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    btnSimpan.isEnabled = true
                    btnSimpan.text = "Simpan"
                }
            }).dispatch()
    }

    private fun saveDataToFirestore(
        title: String,
        desc: String,
        videoUrl: String,
        edtTitle: EditText,
        edtDescription: EditText
    ) {
        btnSimpan.text = "Menyimpan Data..."

        val db = FirebaseFirestore.getInstance()

        val resepBaru = mapOf(
            "title" to title,
            "description" to desc,
            "videoUrl" to videoUrl,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("recipes")
            .add(resepBaru)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Resep & Video Berhasil Disimpan ke Firebase Cloud!", Toast.LENGTH_SHORT).show()

                edtTitle.text.clear()
                edtDescription.text.clear()
                txtVideoPath.text = "Belum ada video yang dipilih"
                selectedVideoUri = null

                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Gagal simpan data ke Firestore: ${exception.message}", Toast.LENGTH_LONG).show()

                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
            }
    }
}