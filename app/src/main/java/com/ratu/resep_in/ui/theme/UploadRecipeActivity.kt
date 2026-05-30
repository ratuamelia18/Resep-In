package com.ratu.resep_in

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class UploadRecipeActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var tvVideoName: TextView

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    companion object {
        private const val IMAGE_REQUEST = 100
        private const val VIDEO_REQUEST = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_recipe)

        val etRecipeName = findViewById<EditText>(R.id.etRecipeName)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etIngredients = findViewById<EditText>(R.id.etIngredients)
        val etSteps = findViewById<EditText>(R.id.etSteps)

        val btnUploadImage = findViewById<Button>(R.id.btnUploadImage)
        val btnUploadVideo = findViewById<Button>(R.id.btnUploadVideo)
        val btnSaveRecipe = findViewById<Button>(R.id.btnSaveRecipe)

        imgPreview = findViewById(R.id.imgPreview)
        tvVideoName = findViewById(R.id.tvVideoName)

        btnUploadImage.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_REQUEST)

        }

        btnUploadVideo.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, VIDEO_REQUEST)

        }

        btnSaveRecipe.setOnClickListener {

            val recipeName = etRecipeName.text.toString()
            val description = etDescription.text.toString()
            val ingredients = etIngredients.text.toString()
            val steps = etSteps.text.toString()

            if (recipeName.isEmpty() ||
                description.isEmpty() ||
                ingredients.isEmpty() ||
                steps.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Lengkapi semua data",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Resep berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {

            when (requestCode) {

                IMAGE_REQUEST -> {

                    imageUri = data.data
                    imgPreview.setImageURI(imageUri)

                }

                VIDEO_REQUEST -> {

                    videoUri = data.data
                    tvVideoName.text = "Video berhasil dipilih"

                }
            }
        }
    }
}