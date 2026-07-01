package com.ratu.resep_in.fragments

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.*
import com.ratu.resep_in.adapter.PostAdapter
import com.ratu.resep_in.databinding.FragmentProfileBinding
import com.ratu.resep_in.detail.DetailRecipeActivity
import com.ratu.resep_in.main.EditRecipeActivity
import com.ratu.resep_in.model.Recipe
import com.ratu.resep_in.profile.EditProfileActivity
import com.ratu.resep_in.profile.FollowListActivity
import com.ratu.resep_in.profile.SettingsActivity
import kotlin.math.sqrt

class ProfileFragment : Fragment(R.layout.fragment_profile), SensorEventListener {

    private lateinit var binding: FragmentProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var postAdapter: PostAdapter

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastRefreshTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setupRecyclerView()
        loadProfileData()
        loadMyPosts()


        binding.imgProfile.setOnClickListener {
            val options = arrayOf("Ambil Foto (Kamera)", "Pilih dari Galeri")
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Ubah Foto Profil")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 101)
                    } else {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, 102)
                    }
                }.show()
        }


        binding.btnSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            intent.putExtra("CURRENT_USERNAME", binding.tvUsername.text.toString())
            intent.putExtra("CURRENT_BIO", binding.tvBio.text.toString())
            startActivity(intent)
        }

        binding.llFollowers.setOnClickListener {
            val intent = Intent(requireContext(), FollowListActivity::class.java)
            intent.putExtra("USER_ID", auth.currentUser?.uid)
            intent.putExtra("TYPE", "FOLLOWERS")
            startActivity(intent)
        }

        binding.llFollowing.setOnClickListener {
            val intent = Intent(requireContext(), FollowListActivity::class.java)
            intent.putExtra("USER_ID", auth.currentUser?.uid)
            intent.putExtra("TYPE", "FOLLOWING")
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {
                101 -> {
                    val bitmap = data?.extras?.get("data") as? android.graphics.Bitmap
                    binding.imgProfile.setImageBitmap(bitmap)

                }
                102 -> {
                    val uri = data?.data
                    binding.imgProfile.setImageURI(uri)

                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - 9.8

            if (acceleration > 15) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastRefreshTime > 2000) {
                    loadProfileData()
                    lastRefreshTime = currentTime
                    Toast.makeText(requireContext(), "Profil di-refresh!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onItemClick = { recipe -> navigateToDetail(recipe) },
            onMoreClick = { recipe, view -> showPopupMenu(recipe, view) }
        )
        binding.rvMyPosts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = postAdapter
        }
    }

    private fun showPopupMenu(recipe: Recipe, view: View) {
        val popup = android.widget.PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_post_options, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    val intent = Intent(requireContext(), EditRecipeActivity::class.java)
                    intent.putExtra("RECIPE_DATA", recipe)
                    startActivity(intent)
                    true
                }
                R.id.action_delete -> {
                    deleteRecipe(recipe.id)
                    true
                }
                R.id.action_archive -> {
                    archiveRecipe(recipe.id)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun deleteRecipe(recipeId: String) {
        FirebaseFirestore.getInstance().collection("resep").document(recipeId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Resep berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadMyPosts()
            }
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .addSnapshotListener { doc, e ->
                if (e != null) return@addSnapshotListener
                if (doc != null && doc.exists()) {
                    binding.tvUsername.text = doc.getString("username") ?: "User"
                    binding.tvBio.text = doc.getString("bio") ?: "Belum ada bio"


                    val followers = doc.getLong("followerCount") ?: 0
                    val following = doc.getLong("followingCount") ?: 0

                    binding.tvCountFollowers.text = followers.toString()
                    binding.tvCountFollowing.text = following.toString()

                    Glide.with(this).load(doc.getString("profileImageUrl"))
                        .placeholder(R.drawable.placeholder_user).into(binding.imgProfile)
                }
            }
    }

    private fun loadMyPosts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("resep")
            .whereEqualTo("authorId", uid)

            .get()
            .addOnSuccessListener { snapshots ->
                val myPosts = snapshots.map { doc ->
                    doc.toObject(Recipe::class.java).apply { id = doc.id }
                }.filter { recipe ->
                    val isArchived = recipe.isArchived ?: false
                    !isArchived
                }

                binding.tvCountResep.text = "${myPosts.size}"
                postAdapter.submitList(myPosts)
            }
    }

    private fun navigateToDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), DetailRecipeActivity::class.java)
        intent.putExtra("RECIPE_DATA", recipe)
        startActivity(intent)
    }

    private fun archiveRecipe(recipeId: String) {
        FirebaseFirestore.getInstance().collection("resep").document(recipeId)
            .update("isArchived", true)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Resep berhasil diarsipkan", Toast.LENGTH_SHORT).show()
                loadMyPosts()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengarsipkan resep", Toast.LENGTH_SHORT).show()
            }
    }


}