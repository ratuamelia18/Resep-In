package com.ratu.resep_in

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.ratu.resep_in.model.Recipe

class DetailRecipeActivity : AppCompatActivity() {

    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvContentTitle: TextView
    private lateinit var tvContentDescription: TextView
    private lateinit var imgVideoThumbnail: ImageView
    private lateinit var btnPlayVideo: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var tabLayoutRecipe: TabLayout

    private var currentRecipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_recipe)

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        tvContentTitle = findViewById(R.id.tvContentTitle)
        tvContentDescription = findViewById(R.id.tvContentDescription)
        imgVideoThumbnail = findViewById(R.id.imgVideoThumbnail)
        btnPlayVideo = findViewById(R.id.btnPlayVideo)
        btnBack = findViewById(R.id.btnBack)
        tabLayoutRecipe = findViewById(R.id.tabLayoutRecipe)

        btnBack.setOnClickListener {
            finish()
        }

        currentRecipe = intent.getSerializableExtra("RECIPE_DATA") as? Recipe

        setupRecipeData()

        setupTabLogic()

        btnPlayVideo.setOnClickListener {
            val videoUrl = currentRecipe?.videoUrl
            if (!videoUrl.isNullOrEmpty()) {
                Toast.makeText(this, "Memutar video tutorial...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Video tutorial tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupRecipeData() {
        currentRecipe?.let { recipe ->
            tvHeaderTitle.text = recipe.title

            Glide.with(this)
                .load(recipe.imgurl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(imgVideoThumbnail)

            showDetailTab()
        } ?: run {
            Toast.makeText(this, "Data resep gagal dimuat", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupTabLogic() {
        tabLayoutRecipe.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showDetailTab()
                    1 -> showIngredientsTab()
                    2 -> showStepsTab()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showDetailTab() {
        currentRecipe?.let { recipe ->
            tvContentTitle.text = "Detail Resep"
            tvContentDescription.text = "Kategori: ${recipe.category}\n" +
                    "Rating: ⭐ ${recipe.rating}\n" +
                    "Waktu Memasak: ${recipe.duration} menit"
        }
    }


    private fun showIngredientsTab() {
        currentRecipe?.let { recipe ->
            tvContentTitle.text = "Bahan-Bahan"

            val ingredientsText = recipe.ingredients.joinToString("\n") { "• $it" }
            tvContentDescription.text = if (ingredientsText.isNotEmpty()) ingredientsText else "Bahan-bahan belum dimasukkan."
        }
    }

    private fun showStepsTab() {
        currentRecipe?.let { recipe ->
            tvContentTitle.text = "Langkah Memasak"

            val stepsText = recipe.steps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n\n")
            tvContentDescription.text = if (stepsText.isNotEmpty()) stepsText else "Langkah-langkah belum dimasukkan."
        }
    }
}