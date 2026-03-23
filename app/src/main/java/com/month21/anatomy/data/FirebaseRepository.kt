package com.month21.anatomy.data

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

// ── Cloudinary 설정 ──
object Cloudinary {
    const val CLOUD_NAME   = "dd52lyfox"
    const val UPLOAD_PRESET = "anatomy_preset"
    val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
}

class FirebaseRepository {
    private val auth   = FirebaseAuth.getInstance()
    private val db     = FirebaseFirestore.getInstance().also {
        it.firestoreSettings = firestoreSettings { isPersistenceEnabled = true }
    }
    private val http   = OkHttpClient()

    val currentUser: FirebaseUser? get() = auth.currentUser
    fun addAuthStateListener(l: FirebaseAuth.AuthStateListener)    = auth.addAuthStateListener(l)
    fun removeAuthStateListener(l: FirebaseAuth.AuthStateListener) = auth.removeAuthStateListener(l)
    suspend fun signOut() = auth.signOut()

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    // ── Bones ──
    suspend fun loadBones(uid: String): List<Bone>? = try {
        val doc = userDoc(uid).get().await()
        if (!doc.exists()) return null
        @Suppress("UNCHECKED_CAST")
        (doc.get("bones") as? List<Map<String, Any>>)?.map { m ->
            Bone(
                id    = m["id"]    as? String  ?: "",
                k     = m["k"]     as? String  ?: "",
                l     = m["l"]     as? String  ?: "",
                axial = m["axial"] as? Boolean ?: true,
                cat   = m["cat"]   as? String  ?: "",
                desc  = m["desc"]  as? String  ?: "",
                tip   = m["tip"]   as? String  ?: "",
                story = m["story"] as? String  ?: ""
            )
        }
    } catch (e: Exception) { null }

    suspend fun saveBones(uid: String, bones: List<Bone>) = try {
        val data = bones.map { mapOf("id" to it.id, "k" to it.k, "l" to it.l,
            "axial" to it.axial, "cat" to it.cat, "desc" to it.desc,
            "tip" to it.tip, "story" to it.story) }
        userDoc(uid).set(mapOf("bones" to data), SetOptions.merge()).await()
    } catch (_: Exception) {}

    // ── Stats ──
    suspend fun loadStats(uid: String): Map<String, Int> = try {
        val doc = userDoc(uid).get().await()
        @Suppress("UNCHECKED_CAST")
        (doc.get("stats") as? Map<String, Long>)?.mapValues { it.value.toInt() } ?: emptyMap()
    } catch (e: Exception) { emptyMap() }

    suspend fun saveStats(uid: String, stats: Map<String, Int>) = try {
        userDoc(uid).set(mapOf("stats" to stats), SetOptions.merge()).await()
    } catch (_: Exception) {}

    // ── Categories ──
    suspend fun loadCategories(uid: String): List<Category>? = try {
        val doc = userDoc(uid).get().await()
        if (!doc.exists()) return null
        @Suppress("UNCHECKED_CAST")
        (doc.get("categories") as? List<Map<String, Any>>)?.map { m ->
            Category(
                name     = m["name"]     as? String  ?: "",
                colorHex = m["colorHex"] as? String  ?: "#6366F1",
                axial    = m["axial"]    as? Boolean ?: true,
                order    = (m["order"]   as? Long)?.toInt() ?: 0
            )
        }
    } catch (e: Exception) { null }

    suspend fun saveCategories(uid: String, cats: List<Category>) = try {
        val data = cats.map { mapOf("name" to it.name, "colorHex" to it.colorHex,
            "axial" to it.axial, "order" to it.order) }
        userDoc(uid).set(mapOf("categories" to data), SetOptions.merge()).await()
    } catch (_: Exception) {}

    // ── Images (Cloudinary) ──
    // imageUrls: { boneId -> cloudinary_url } → Firestore에 저장

    suspend fun loadImageUrls(uid: String): Map<String, String> = try {
        val doc = userDoc(uid).get().await()
        @Suppress("UNCHECKED_CAST")
        (doc.get("imageUrls") as? Map<String, String>) ?: emptyMap()
    } catch (e: Exception) { emptyMap() }

    /**
     * 갤러리 URI → Cloudinary 업로드 → URL 반환
     * public_id를 "{uid}_{boneId}" 로 지정해서 덮어쓰기(교체) 지원
     */
    suspend fun uploadImage(context: Context, uid: String, boneId: String, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                // URI → 임시 파일
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val tmpFile = File(context.cacheDir, "upload_$boneId.jpg")
                FileOutputStream(tmpFile).use { out -> inputStream.copyTo(out) }

                // Cloudinary multipart upload
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", Cloudinary.UPLOAD_PRESET)
                    .addFormDataPart("public_id", "${uid}_${boneId}")  // 같은 뼈 업로드 시 덮어쓰기
                    .addFormDataPart("file", tmpFile.name,
                        tmpFile.asRequestBody("image/*".toMediaType()))
                    .build()

                val request = Request.Builder()
                    .url(Cloudinary.UPLOAD_URL)
                    .post(body)
                    .build()

                val response = http.newCall(request).execute()
                val json = JSONObject(response.body?.string() ?: return@withContext null)

                // 임시 파일 삭제
                tmpFile.delete()

                json.optString("secure_url").takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun saveImageUrl(uid: String, boneId: String, url: String) = try {
        userDoc(uid).set(mapOf("imageUrls" to mapOf(boneId to url)), SetOptions.merge()).await()
    } catch (_: Exception) {}

    suspend fun removeImageUrl(uid: String, boneId: String) = try {
        userDoc(uid).update("imageUrls.$boneId", FieldValue.delete()).await()
    } catch (_: Exception) {}
}
