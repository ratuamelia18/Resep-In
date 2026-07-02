package com.ratu.resep_in.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ratu.resep_in.R
import com.ratu.resep_in.adapter.UserAdapter
import com.ratu.resep_in.model.User

class FollowListActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        val targetUserId = intent.getStringExtra("USER_ID") ?: return
        val type = intent.getStringExtra("TYPE")

        findViewById<Toolbar>(R.id.toolbar).title = type

        rvUsers = findViewById(R.id.rvUsers)
        rvUsers.layoutManager = LinearLayoutManager(this)

        userAdapter = UserAdapter(emptyList(), { user ->
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            startActivity(intent)
        }, { })

        rvUsers.adapter = userAdapter

        if (type == "FOLLOWERS") loadFollowers(targetUserId) else loadFollowing(targetUserId)
    }

    private fun loadFollowers(userId: String) {
        db.collection("follows").whereEqualTo("followedId", userId).get()
            .addOnSuccessListener { snapshots ->
                val ids = snapshots.map { it.getString("followerId") ?: "" }
                fetchUserDetails(ids)
            }
    }

    private fun loadFollowing(userId: String) {
        db.collection("follows").whereEqualTo("followerId", userId).get()
            .addOnSuccessListener { snapshots ->
                val ids = snapshots.map { it.getString("followedId") ?: "" }
                fetchUserDetails(ids)
            }
    }

    private fun fetchUserDetails(ids: List<String>) {
        if (ids.isEmpty()) return
        db.collection("users")
            .whereIn(FieldPath.documentId(), ids)
            .get()
            .addOnSuccessListener { snapshots ->
                val userList = snapshots.toObjects(User::class.java)
                userAdapter.updateData(userList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}