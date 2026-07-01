package com.ratu.resep_in.main

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R
import com.ratu.resep_in.model.Recipe
import kotlin.collections.get

class EditRecipeActivity : AppCompatActivity() {

    private var selectedVideoUri: Uri? = null
    private var selectedFotoUri: Uri? = null
    private var currentRecipe: Recipe? = null
    private val db = FirebaseFirestore.getInstance()

    private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { selectedVideoUri = uri; Toast.makeText(this, "Video terpilih!", Toast.LENGTH_SHORT).show() }
    }
    private val pickFoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { selectedFotoUri = uri; Toast.makeText(this, "Foto terpilih!", Toast.LENGTH_SHORT).show() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        currentRecipe = intent.getSerializableExtra("RECIPE_DATA") as? Recipe

        if (currentRecipe == null) {
            Toast.makeText(this, "Data resep tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<EditText>(R.id.etNamaMasakan).setText(currentRecipe!!.title)
        findViewById<EditText>(R.id.etAlatBahan).setText(currentRecipe!!.ingredients.joinToString("\n"))
        findViewById<EditText>(R.id.etLangkah).setText(currentRecipe!!.steps.joinToString("\n"))

        val spinnerKategori = findViewById<Spinner>(R.id.spinnerKategori)
        val kategoriList = listOf(
            "Pilih Kategori", "Daging", "Ayam & Bebek", "Seafood",
            "Tahu & Tempe", "Telur", "Buah", "Sayur", "Nasi", "Mie"
        )
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategoriList)
        spinnerKategori.adapter = adapter

        val kategoriIndex = kategoriList.indexOf(currentRecipe!!.category)
        if (kategoriIndex >= 0) spinnerKategori.setSelection(kategoriIndex)

        findViewById<FrameLayout>(R.id.btnUploadVideo).setOnClickListener { pickVideo.launch("video/*") }
        findViewById<FrameLayout>(R.id.btnUploadFoto).setOnClickListener { pickFoto.launch("image/*") }

        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        btnSubmit.text = "Simpan Perubahan"
        btnSubmit.setOnClickListener {
            val nama = findViewById<EditText>(R.id.etNamaMasakan).text.toString().trim()
            val bahan = findViewById<EditText>(R.id.etAlatBahan).text.toString().trim()
            val langkah = findViewById<EditText>(R.id.etLangkah).text.toString().trim()
            val kategori = spinnerKategori.selectedItem.toString()

            if (nama.isEmpty() || bahan.isEmpty() || langkah.isEmpty() || kategori == "Pilih Kategori") {
                Toast.makeText(this, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prosesEdit(nama, kategori, bahan, langkah)
        }
    }

    private fun prosesEdit(nama: String, kategori: String, bahan: String, langkah: String) {
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        btnSubmit.isEnabled = false
        btnSubmit.text = "Menyimpan..."

        if (selectedFotoUri != null) {
            uploadKeCloudinary(selectedFotoUri!!, false) { urlFoto ->
                if (selectedVideoUri != null) {
                    uploadKeCloudinary(selectedVideoUri!!, true) { urlVideo ->
                        simpanPerubahan(nama, kategori, bahan, langkah, urlFoto, urlVideo)
                    }
                } else {
                    simpanPerubahan(nama, kategori, bahan, langkah, urlFoto, currentRecipe!!.videoUrl)
                }
            }
        } else {
            if (selectedVideoUri != null) {
                uploadKeCloudinary(selectedVideoUri!!, true) { urlVideo ->
                    simpanPerubahan(nama, kategori, bahan, langkah, currentRecipe!!.imgurl, urlVideo)
                }
            } else {
                simpanPerubahan(nama, kategori, bahan, langkah, currentRecipe!!.imgurl, currentRecipe!!.videoUrl)
            }
        }
    }

    private fun uploadKeCloudinary(uri: Uri, isVideo: Boolean, callback: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .option("resource_type", if (isVideo) "video" else "image")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    callback(resultData["secure_url"].toString())
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        findViewById<Button>(R.id.btnSubmit).isEnabled = true
                        findViewById<Button>(R.id.btnSubmit).text = "Simpan Perubahan"
                        Toast.makeText(this@EditRecipeActivity, "Gagal upload: ${error.description}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onStart(id: String) {}
                override fun onProgress(id: String, b: Long, t: Long) {}
                override fun onReschedule(id: String, e: ErrorInfo) {}
            }).dispatch()
    }

    private fun simpanPerubahan(nama: String, kat: String, bah: String, lan: String, img: String, vid: String) {
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        if (currentRecipe?.id == null) {
            Toast.makeText(this, "Error: ID Resep tidak ditemukan!", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val data = hashMapOf<String, Any>(
                "title" to nama,
                "kategori" to kat,
                "bahan" to bah.split("\n").filter { it.isNotBlank() },
                "langkah" to lan.split("\n").filter { it.isNotBlank() },
                "foto" to img,
                "videoUrl" to vid
            )

            db.collection("resep").document(currentRecipe!!.id)
                .update(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Resep berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Simpan Perubahan"
                    Toast.makeText(this, "Gagal Firestore: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}