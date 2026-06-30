package com.ratu.resep_in

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import com.bumptech.glide.Glide
class EditProfileActivity : AppCompatActivity() {

    private lateinit var etEditUsername: EditText
    private lateinit var etEditBio: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var imgEditProfile: CircleImageView
    private lateinit var btnChangePhoto: ImageView

    private var isUsernameValid = true
    private var currentUsername = ""
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etEditUsername = findViewById(R.id.etEditUsername)
        etEditBio = findViewById(R.id.etEditBio)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        imgEditProfile = findViewById(R.id.imgEditProfile)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)

        loadCurrentProfile()


        btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        etEditUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                if (input.isNotEmpty() && input != currentUsername) {
                    checkUsernameAvailability(input)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSaveProfile.setOnClickListener {
            if (isUsernameValid) {
                btnSaveProfile.isEnabled = false
                if (imageUri != null) {
                    uploadToCloudinary(imageUri!!)
                } else {
                    saveProfileToFirestore(null)
                }
            } else {
                Toast.makeText(this, "Username sudah dipakai!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imgEditProfile.setImageURI(imageUri)
        }
    }

    private fun uploadToCloudinary(uri: Uri) {
        MediaManager.get().upload(uri).callback(object : UploadCallback {
            override fun onStart(requestId: String?) {}
            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                val imageUrl = resultData?.get("secure_url").toString()
                saveProfileToFirestore(imageUrl)
            }
            override fun onError(requestId: String?, error: ErrorInfo?) {
                btnSaveProfile.isEnabled = true
                Toast.makeText(this@EditProfileActivity, "Gagal upload foto: ${error?.description}", Toast.LENGTH_SHORT).show()
            }
            override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
        }).dispatch()
    }

    private fun saveProfileToFirestore(imageUrl: String?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>(
            "username" to etEditUsername.text.toString(),
            "bio" to etEditBio.text.toString()
        )
        if (imageUrl != null) updates["profileImageUrl"] = imageUrl

        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                if (imageUrl != null) {
                    updateProfilePhotoInComments(imageUrl)
                } else {
                    Toast.makeText(this, "Profil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                btnSaveProfile.isEnabled = true
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfilePhotoInComments(newPhotoUrl: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("comments").whereEqualTo("userId", uid).get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()
                for (doc in snapshots.documents) {
                    batch.update(doc.reference, "userPhoto", newPhotoUrl)
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "Profil & foto komentar diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Profil sukses, tapi foto komentar gagal update", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }
    private fun loadCurrentProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                currentUsername = doc.getString("username") ?: ""
                etEditUsername.setText(currentUsername)
                etEditBio.setText(doc.getString("bio") ?: "")

                val photoUrl = doc.getString("profileImageUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.placeholder_user)
                        .into(imgEditProfile)
                }
            }
    }
    private fun checkUsernameAvailability(username: String) {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                isUsernameValid = documents.isEmpty
            }
    }
}