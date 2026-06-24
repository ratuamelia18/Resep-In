package com.ratu.resep_in.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.EditRecipeActivity
import com.ratu.resep_in.DetailRecipeActivity
import com.ratu.resep_in.LoginActivity
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.PostAdapter
import com.ratu.resep_in.databinding.FragmentProfileBinding
import com.ratu.resep_in.model.Recipe
import androidx.recyclerview.widget.GridLayoutManager

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var postAdapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        setupRecyclerView()
        loadProfileData()
        loadMyPosts()

        binding.btnSettings.setOnClickListener {
            showLogoutDialog()
        }
    }

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
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.tvUsername.text = doc.getString("username") ?: "User"
                    binding.tvBio.text = doc.getString("bio") ?: "Belum ada bio"

                    val photoUrl = doc.getString("profileImageUrl")
                    Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.placeholder_user)
                        .into(binding.imgProfile)
                }
            }
    }

    private fun loadMyPosts() {
        android.util.Log.e("DEBUG_DATA", "Fungsi loadMyPosts mulai berjalan!")

        val uid = auth.currentUser?.uid
        if (uid == null) {
            android.util.Log.e("DEBUG_DATA", "User tidak login!")
            return
        }

        db.collection("resep")
            .whereEqualTo("authorId", uid)
            .get()
            .addOnSuccessListener { snapshots ->
                val myPosts = mutableListOf<Recipe>()
                for (doc in snapshots.documents) {
                    val recipe = doc.toObject(Recipe::class.java)
                    recipe?.let {
                        it.id = doc.id
                        myPosts.add(it)
                    }
                }
                binding.tvCountResep.text = "${myPosts.size}"
                postAdapter.submitList(myPosts)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("DEBUG_DATA", "Error Firestore: ${e.message}")
            }
    }

    private fun showLogoutDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ -> logoutUser() }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun navigateToDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), DetailRecipeActivity::class.java)
        intent.putExtra("RECIPE_DATA", recipe)
        startActivity(intent)
    }



    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }


}