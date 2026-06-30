package com.ratu.resep_in.ui.theme

import com.ratu.resep_in.DetailRecipeActivity
import com.ratu.resep_in.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.databinding.ActivityOtherProfileBinding
import com.ratu.resep_in.adapter.PostAdapter
import com.ratu.resep_in.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.ratu.resep_in.FollowListActivity

class OtherProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtherProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var postAdapter: PostAdapter
    private var targetUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetUserId = intent.getStringExtra("USER_ID")
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        if (targetUserId == null) {
            finish()
            return
        }

        if (targetUserId == currentUid) {
            binding.btnFollow.visibility = View.GONE
        } else {
            binding.btnFollow.visibility = View.VISIBLE
            checkFollowStatus()
        }

        binding.btnFollow.setOnClickListener {
            targetUserId?.let { id -> toggleFollow(id) }
        }

        binding.llFollowers.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("USER_ID", targetUserId)
            intent.putExtra("TYPE", "FOLLOWERS")
            startActivity(intent)
        }

        binding.llFollowing.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("USER_ID", targetUserId)
            intent.putExtra("TYPE", "FOLLOWING")
            startActivity(intent)
        }

        setupRecyclerView()
        loadOtherProfileData()
        loadOtherPosts()
    }
    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onItemClick = { recipe -> navigateToDetail(recipe) },
            onMoreClick = { _, _ -> }
        )
        binding.rvMyPosts.apply {
            layoutManager = GridLayoutManager(this@OtherProfileActivity, 2)
            adapter = postAdapter
        }
    }

    private fun loadOtherProfileData() {
        db.collection("users").document(targetUserId!!).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
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

    private fun loadOtherPosts() {
        db.collection("resep").whereEqualTo("authorId", targetUserId).get()
            .addOnSuccessListener { snapshots ->
                val posts = snapshots.map { doc -> doc.toObject(Recipe::class.java).apply { id = doc.id } }
                binding.tvCountResep.text = "${posts.size}"
                postAdapter.submitList(posts)
            }
    }

    private fun navigateToDetail(recipe: Recipe) {
        val intent = Intent(this, DetailRecipeActivity::class.java)
        intent.putExtra("RECIPE_DATA", recipe)
        startActivity(intent)
    }

    private fun checkFollowStatus() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val followId = "${currentUid}_${targetUserId}"

        db.collection("follows").document(followId).get()
            .addOnSuccessListener { doc ->
                updateButtonUI(doc.exists())
            }
    }

    private fun toggleFollow(targetUserId: String) {
        if (targetUserId == null) return
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val followId = "${currentUid}_${targetUserId}"
        val followRef = db.collection("follows").document(followId)

        val meRef = db.collection("users").document(currentUid)
        val targetRef = db.collection("users").document(targetUserId)

        followRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                followRef.delete().addOnSuccessListener {
                    updateButtonUI(false)
                }
                meRef.update("followingCount", com.google.firebase.firestore.FieldValue.increment(-1))
                targetRef.update("followerCount", com.google.firebase.firestore.FieldValue.increment(-1))
                Toast.makeText(this, "Berhenti mengikuti", Toast.LENGTH_SHORT).show()
            } else {
                val followData = mapOf("followerId" to currentUid, "followedId" to targetUserId)
                followRef.set(followData).addOnSuccessListener {
                    updateButtonUI(true)
                }
                meRef.update("followingCount", com.google.firebase.firestore.FieldValue.increment(1))
                targetRef.update("followerCount", com.google.firebase.firestore.FieldValue.increment(1))
                Toast.makeText(this, "Mengikuti!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateButtonUI(isFollowing: Boolean) {
        if (isFinishing || isDestroyed) return
        if (isFollowing) {
            binding.btnFollow.text = "Mengikuti"
            binding.btnFollow.setBackgroundColor(getColor(R.color.gray))
        } else {
            binding.btnFollow.text = "Follow"
            binding.btnFollow.setBackgroundColor(getColor(R.color.red_primary))
        }
    }
}