package com.example.selling_used_market.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.selling_used_market.DBKey.Companion.DB_ARTICLES
import com.example.selling_used_market.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

@Suppress("DEPRECATION")
class AddArticleActivity: AppCompatActivity() {

    private val itemImageView: ImageView by lazy {
        findViewById<ImageButton>(R.id.itemImageView)
    }
    private val upLoadImageButton: Button by lazy {
        findViewById(R.id.uploadImageButton)
    }
    private val titleEditText: EditText by lazy {
        findViewById<EditText>(R.id.titleEditText)
    }
    private val priceEditText: EditText by lazy {
        findViewById(R.id.priceEditText)
    }
    private val completeButton: Button by lazy {
        findViewById<Button>(R.id.completeButton)
    }

    private var selectedUri: Uri? = null

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_ARTICLES)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)

        upLoadImageButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, STORAGE_PERMISSION)
                        == PackageManager.PERMISSION_GRANTED -> { /*권한이 허용된 경우*/
                    startContentProvider()
                }
                shouldShowRequestPermissionRationale(STORAGE_PERMISSION) -> { /*권한을 거절한 경우*/
                    showPermissionContextPopup()
                }
                else -> {  /*처음 권한을 요청*/
                    requestPermissions(arrayOf(STORAGE_PERMISSION), REQUEST_CODE)
                }
            }
        }

        completeButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val price = priceEditText.text.toString()
            val sellerId = auth.currentUser?.uid.orEmpty()

            showProgress()

            if (selectedUri != null) {
                val imageUri = selectedUri ?: return@setOnClickListener
                uploadImageToStorage(imageUri,
                    successHandler = { uri ->
                        uploadArticle(sellerId, title, price, uri)
                    },
                    errorHandler = {
                        hideProgress()
                        Toast.makeText(this, "사진압로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                uploadArticle(sellerId, title, price, "")
            }

            val model = ArticleModel(sellerId, title, System.currentTimeMillis(), "{$price}원", "")
            articleDB.push().setValue(model)

            finish()
        }
    }

    private fun uploadImageToStorage(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val sellerId = auth.currentUser?.uid.orEmpty()
        val fileName = "${sellerId}_${System.currentTimeMillis()}.png"
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    storage.reference.child("article/photo").child(fileName)
                        .downloadUrl.addOnSuccessListener { uri ->
                         successHandler(uri.toString())
                        }.addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }

    private fun uploadArticle(sellerId: String, title: String, price: String, imageUrl: String) {
        val model = ArticleModel(sellerId, title, System.currentTimeMillis(), "{$price}원", imageUrl)
        articleDB.push().setValue(model)

        hideProgress()
        finish()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showProgress() {
        findViewById<ProgressBar>(R.id.progressBar).isVisible = true
    }

    private fun hideProgress() {
        findViewById<ProgressBar>(R.id.progressBar).isVisible = false
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, INTENT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            INTENT_REQUEST_CODE -> {
                val uri = data?.data
                if (uri != null) {
                    itemImageView.setImageURI(uri)
                    selectedUri = uri
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this).setTitle("권한이 필요합니다.")
            .setMessage("사진등록")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(arrayOf(STORAGE_PERMISSION), REQUEST_CODE)
            }
            .create().show()
    }

    companion object {
        const val STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        const val REQUEST_CODE = 1000
        const val INTENT_REQUEST_CODE = 2000
    }
}