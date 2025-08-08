package edu.unikom.herbamedjabar.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.unikom.herbamedjabar.data.Post
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore
    // FirebaseStorage tidak lagi diinject di sini
) {

    /**
     * Mengambil daftar postingan dari Firestore secara real-time.
     * Postingan diurutkan berdasarkan waktu terbaru.
     */
    fun getPosts(): Flow<List<Post>> = callbackFlow {
        val collection = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Menutup flow jika terjadi error
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Konversi dokumen snapshot menjadi daftar objek Post
                val posts = snapshot.toObjects(Post::class.java)
                trySend(posts).isSuccess // Mengirim data terbaru ke flow
            }
        }
        // Menghapus listener saat flow ditutup untuk menghindari memory leak
        awaitClose { listener.remove() }
    }

    /**
     * Membuat postingan baru.
     * Proses ini mencakup pengunggahan gambar ke Cloudinary
     * dan penyimpanan data postingan ke Firestore.
     */
    suspend fun createPost(
        userId: String,
        username: String,
        userProfilePictureUrl: String?,
        imageUri: Uri,
        plantName: String,
        description: String
    ) {
        // 1. Unggah gambar ke Cloudinary dan dapatkan URL-nya
        val imageUrl = uploadImageToCloudinary(imageUri)

        // 2. Buat objek Post dengan data yang relevan
        val postId = firestore.collection("posts").document().id
        val newPost = Post(
            id = postId,
            userId = userId,
            username = username,
            userProfilePictureUrl = userProfilePictureUrl,
            imageUrl = imageUrl,
            plantName = plantName,
            description = description,
            timestamp = System.currentTimeMillis()
        )

        // 3. Simpan objek Post ke koleksi "posts" di Firestore
        firestore.collection("posts").document(postId).set(newPost).await()
    }

    /**
     * Fungsi bantuan untuk mengunggah gambar ke Cloudinary.
     * Menggunakan suspendCancellableCoroutine untuk membungkus callback-based API
     * menjadi sebuah suspend function yang bisa dibatalkan.
     */
    private suspend fun uploadImageToCloudinary(imageUri: Uri): String = suspendCancellableCoroutine { continuation ->
        val requestId = MediaManager.get().upload(imageUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null && continuation.isActive) {
                        continuation.resume(url)
                    } else if (continuation.isActive) {
                        continuation.resumeWithException(Exception("Cloudinary upload failed: URL is null"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(Exception("Cloudinary Error: ${error.description}"))
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()

        // Jika coroutine dibatalkan, batalkan juga request unggah ke Cloudinary
        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }

    suspend fun toggleLike(postId: String, userId: String) {
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likes = snapshot.get("likes") as? List<String> ?: emptyList()
            if (likes.contains(userId)) {
                // Jika sudah me-like, hapus like (unlike)
                transaction.update(postRef, "likes", FieldValue.arrayRemove(userId))
            } else {
                // Jika belum me-like, tambahkan like
                transaction.update(postRef, "likes", FieldValue.arrayUnion(userId))
            }
        }.await()
    }
}
