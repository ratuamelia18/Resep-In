package com.ratu.resep_in

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class UploadActivity : AppCompatActivity() {

    private var selectedVideoUri: Uri? = null
    private var selectedFotoUri: Uri? = null

    private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
            Toast.makeText(this, "Video terpilih!", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickFoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedFotoUri = uri
            Toast.makeText(this, "Foto terpilih!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val spinnerKategori = findViewById<Spinner>(R.id.spinnerKategori)
        val kategoriList = listOf(
            "Pilih Kategori", "Daging", "Ayam & Bebek", "Seafood",
            "Tahu & Tempe", "Telur", "Buah", "Sayur", "Nasi", "Mie"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategoriList)
        spinnerKategori.adapter = adapter

        findViewById<FrameLayout>(R.id.btnUploadVideo).setOnClickListener { pickVideo.launch("video/*") }
        findViewById<FrameLayout>(R.id.btnUploadFoto).setOnClickListener { pickFoto.launch("image/*") }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val nama = findViewById<EditText>(R.id.etNamaMasakan).text.toString().trim()
            val bahan = findViewById<EditText>(R.id.etAlatBahan).text.toString().trim()
            val langkah = findViewById<EditText>(R.id.etLangkah).text.toString().trim()
            val kategori = spinnerKategori.selectedItem.toString()

            if (nama.isEmpty() || bahan.isEmpty() || langkah.isEmpty() || kategori == "Pilih Kategori") {
                Toast.makeText(this, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
            } else if (selectedFotoUri == null) {
                Toast.makeText(this, "Foto wajib dipilih!", Toast.LENGTH_SHORT).show()
            } else {
                prosesUpload(nama, kategori, bahan, langkah)
            }
        }
    }

    private fun prosesUpload(nama: String, kategori: String, bahan: String, langkah: String) {
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        btnSubmit.isEnabled = false
        btnSubmit.text = "Mengunggah..."

        uploadKeCloudinary(selectedFotoUri!!, false) { urlFoto ->
            if (selectedVideoUri != null) {
                uploadKeCloudinary(selectedVideoUri!!, true) { urlVideo ->
                    simpanKeFirebase(nama, kategori, bahan, langkah, urlFoto, urlVideo)
                }
            } else {
                simpanKeFirebase(nama, kategori, bahan, langkah, urlFoto, "")
            }
        }
    }

    private fun uploadKeCloudinary(uri: Uri, isVideo: Boolean, callback: (String) -> Unit) {
        val uploadRequest = MediaManager.get().upload(uri)

        if (isVideo) {
            uploadRequest.option("resource_type", "video")
            uploadRequest.option("transformation", "c_limit,w_1280,h_720,q_auto:low")
        } else {
            uploadRequest.option("resource_type", "image")
        }

        uploadRequest.callback(object : UploadCallback {
            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                callback(resultData["secure_url"].toString())
            }
            override fun onError(requestId: String, error: ErrorInfo) {
                Toast.makeText(this@UploadActivity, "Gagal: ${error.description}", Toast.LENGTH_LONG).show()
                findViewById<Button>(R.id.btnSubmit).isEnabled = true
                findViewById<Button>(R.id.btnSubmit).text = "Simpan"
            }
            override fun onStart(id: String) {}
            override fun onProgress(id: String, b: Long, t: Long) {}
            override fun onReschedule(id: String, e: ErrorInfo) {}
        }).dispatch()
    }

    private fun simpanKeFirebase(nama: String, kat: String, bah: String, lan: String, img: String, vid: String) {
        val db = FirebaseFirestore.getInstance()
        val data = mapOf(
            "nama" to nama, "kategori" to kat, "bahan" to bah,
            "langkah" to lan, "foto" to img, "video" to vid,
            "timestamp" to Timestamp.now()
        )

        db.collection("resep").add(data).addOnSuccessListener {
            Toast.makeText(this, "Resep berhasil disimpan!", Toast.LENGTH_LONG).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal simpan ke database!", Toast.LENGTH_SHORT).show()
            findViewById<Button>(R.id.btnSubmit).isEnabled = true
            findViewById<Button>(R.id.btnSubmit).text = "Simpan"
        }
    }
}