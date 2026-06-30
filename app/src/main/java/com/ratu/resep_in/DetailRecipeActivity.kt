package com.ratu.resep_in

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.tabs.TabLayout
import com.ratu.resep_in.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.RecyclerView
import com.ratu.resep_in.adapter.CommentAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import android.util.Log

class DetailRecipeActivity : AppCompatActivity() {

    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvContentTitle: TextView
    private lateinit var tvContentDescription: TextView
    private lateinit var playerView: PlayerView
    private lateinit var btnBack: ImageView
    private lateinit var tabLayoutRecipe: TabLayout

    private lateinit var rvComments: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var etUserComment: EditText
    private lateinit var btnSendComment: ImageView
    private lateinit var rbUserRating: RatingBar
    private lateinit var tvCommentCountTitle: TextView

    private var player: ExoPlayer? = null
    private var currentRecipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_recipe)

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        tvContentTitle = findViewById(R.id.tvContentTitle)
        tvContentDescription = findViewById(R.id.tvContentDescription)
        playerView = findViewById(R.id.playerView)
        btnBack = findViewById(R.id.btnBack)
        tabLayoutRecipe = findViewById(R.id.tabLayoutRecipe)
        tvCommentCountTitle = findViewById(R.id.tvCommentCountTitle)

        currentRecipe = intent.getSerializableExtra("RECIPE_DATA") as? Recipe

        if (currentRecipe == null) {
            Toast.makeText(this, "Gagal memuat detail resep", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnBack.setOnClickListener { finish() }

        setupRecipeData()
        setupTabLogic()
        setupPlayer()

        setupComments()
    }

    private fun setupComments() {
        rvComments = findViewById(R.id.rvComments)
        etUserComment = findViewById(R.id.etUserComment)
        btnSendComment = findViewById(R.id.btnSendComment)
        rbUserRating = findViewById(R.id.rbUserRating)
        tvCommentCountTitle = findViewById(R.id.tvCommentCountTitle)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        commentAdapter = CommentAdapter(
            commentList = emptyList(),
            currentUserUid = currentUserId,
            onDeleteClick = { commentToDelete ->
                deleteCommentFromFirestore(commentToDelete)
            },
            onUserClick = { userId ->
                if (userId != currentUserId) {
                    val intent = android.content.Intent(this, com.ratu.resep_in.ui.theme.OtherProfileActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Ini profil Anda", Toast.LENGTH_SHORT).show()
                }
            }
        )

        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        currentRecipe?.let { loadComments(it.id) }

        btnSendComment.setOnClickListener {
            val rating = rbUserRating.rating
            val commentText = etUserComment.text.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null && rating > 0 && commentText.isNotEmpty()) {
                postCommentAndRating(currentRecipe!!.id, userId, rating, commentText)
            } else {
                Toast.makeText(this, "Berikan rating dan komentar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCommentFromFirestore(comment: com.ratu.resep_in.model.Comment) {
        val db = FirebaseFirestore.getInstance()
        val recipeRef = db.collection("resep").document(comment.recipeId)
        val commentRef = db.collection("comments").document(comment.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(recipeRef)
            val currentAvg = snapshot.getDouble("averageRating") ?: 0.0
            val currentCount = snapshot.getLong("ratingCount") ?: 0L

            if (currentCount > 0) {
                val totalRatingLama = currentAvg * currentCount
                val newCount = currentCount - 1
                val newAvg = if (newCount > 0) (totalRatingLama - comment.rating.toDouble()) / newCount else 0.0

                transaction.update(recipeRef, "averageRating", newAvg, "ratingCount", newCount)
                transaction.delete(commentRef)
                return@runTransaction newAvg
            }
            null
        }.addOnSuccessListener { newAvg ->
            Toast.makeText(this, "Komentar dihapus", Toast.LENGTH_SHORT).show()
            if (newAvg != null) {
                currentRecipe?.averageRating = newAvg
                showDetailTab()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun postCommentAndRating(recipeId: String, userId: String, rating: Float, commentText: String) {
        if (recipeId.isEmpty()) return

        val db = FirebaseFirestore.getInstance()
        val recipeRef = db.collection("resep").document(recipeId)

        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            val username = doc.getString("username") ?: "User"
            val userPhoto = doc.getString("profileImageUrl") ?: ""
            val commentRef = db.collection("comments").document()

            val commentData = mapOf(
                "userId" to userId,
                "username" to username,
                "userPhoto" to userPhoto,
                "comment" to commentText,
                "rating" to rating,
                "recipeId" to recipeId,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            db.runTransaction { transaction ->
                val snapshot = transaction.get(recipeRef)
                if (!snapshot.exists()) throw Exception("Resep tidak ditemukan!")

                val currentAvg = snapshot.getDouble("averageRating") ?: 0.0
                val currentCount = snapshot.getLong("ratingCount") ?: 0L

                val newCount = currentCount + 1
                val newAvg = ((currentAvg * currentCount) + rating) / newCount

                transaction.update(recipeRef, "averageRating", newAvg, "ratingCount", newCount)
                transaction.set(commentRef, commentData)
            }.addOnSuccessListener {
                etUserComment.text.clear()
                rbUserRating.rating = 0f
                Toast.makeText(this, "Komentar terkirim!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadComments(recipeId: String) {
        FirebaseFirestore.getInstance().collection("comments")
            .whereEqualTo("recipeId", recipeId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val comments = snapshots?.toObjects(com.ratu.resep_in.model.Comment::class.java) ?: emptyList()
                tvCommentCountTitle.text = "Komentar Pengguna (${comments.size})"
                commentAdapter.updateData(comments)
            }
    }

    private fun setupPlayer() {
        val videoUrl = currentRecipe?.videoUrl
        if (videoUrl.isNullOrEmpty()) return
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        val mediaItem = MediaItem.fromUri(videoUrl)
        player?.setMediaItem(mediaItem)
        player?.prepare()
    }

    private fun setupRecipeData() {
        currentRecipe?.let { recipe ->
            tvHeaderTitle.text = recipe.title
            showDetailTab()
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
            tvContentTitle.text = recipe.title
            tvContentTitle.textSize = 24f
            val formattedRating = String.format("%.2f", recipe.averageRating)
            val baseInfo = "Kategori: ${recipe.category}\nRating: ⭐ $formattedRating\nWaktu: ${recipe.duration} menit"
            if (!recipe.authorId.isNullOrEmpty()) {
                FirebaseFirestore.getInstance().collection("users").document(recipe.authorId).get()
                    .addOnSuccessListener { doc -> tvContentDescription.text = "Oleh: ${doc.getString("username") ?: "Anonim"}\n$baseInfo" }
            } else {
                tvContentDescription.text = "Oleh: Anonim\n$baseInfo"
            }
        }
    }

    private fun showIngredientsTab() {
        currentRecipe?.let { recipe ->
            tvContentTitle.text = "Bahan-Bahan"
            tvContentTitle.textSize = 18f
            tvContentDescription.text = recipe.ingredients.joinToString("\n") { "• $it" }.ifEmpty { "Bahan belum tersedia." }
        }
    }

    private fun showStepsTab() {
        currentRecipe?.let { recipe ->
            tvContentTitle.text = "Langkah Memasak"
            tvContentTitle.textSize = 18f
            tvContentDescription.text = recipe.steps.mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n\n").ifEmpty { "Langkah belum tersedia." }
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}