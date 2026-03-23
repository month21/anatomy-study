package com.month21.anatomy.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.month21.anatomy.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SyncStatus { IDLE, SAVING, SAVED }

// AndroidViewModel 사용 → Application Context 접근 가능 (Cloudinary 업로드용)
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo    = FirebaseRepository()
    private val context = app.applicationContext

    private val _user       = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    private val _bones      = MutableStateFlow(DEFAULT_BONES)
    val bones: StateFlow<List<Bone>> = _bones

    private val _stats      = MutableStateFlow<Map<String, Int>>(emptyMap())
    val stats: StateFlow<Map<String, Int>> = _stats

    private val _categories = MutableStateFlow(DEFAULT_CATEGORIES)
    val categories: StateFlow<List<Category>> = _categories

    private val _imageUrls  = MutableStateFlow<Map<String, String>>(emptyMap())
    val imageUrls: StateFlow<Map<String, String>> = _imageUrls

    private val _loading    = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _sync       = MutableStateFlow(SyncStatus.IDLE)
    val sync: StateFlow<SyncStatus> = _sync

    private val _uploadingIds = MutableStateFlow<Set<String>>(emptySet())
    val uploadingIds: StateFlow<Set<String>> = _uploadingIds

    private val authListener = FirebaseAuth.AuthStateListener { fa ->
        _user.value = fa.currentUser
        fa.currentUser?.let { u -> viewModelScope.launch { loadFromFirebase(u.uid) } }
    }

    init { repo.addAuthStateListener(authListener) }

    private suspend fun loadFromFirebase(uid: String) {
        _loading.value = true
        val remoteBones = repo.loadBones(uid)
        if (!remoteBones.isNullOrEmpty()) _bones.value = remoteBones
        else repo.saveBones(uid, DEFAULT_BONES)
        _stats.value = repo.loadStats(uid)
        val remoteCats = repo.loadCategories(uid)
        if (!remoteCats.isNullOrEmpty()) _categories.value = remoteCats
        else repo.saveCategories(uid, DEFAULT_CATEGORIES)
        _imageUrls.value = repo.loadImageUrls(uid)
        _loading.value = false
    }

    private fun showSync(block: suspend () -> Unit) {
        viewModelScope.launch {
            _sync.value = SyncStatus.SAVING
            block()
            _sync.value = SyncStatus.SAVED
            delay(2000); _sync.value = SyncStatus.IDLE
        }
    }

    fun saveBones(bones: List<Bone>) {
        _bones.value = bones
        showSync { _user.value?.uid?.let { repo.saveBones(it, bones) } }
    }

    fun saveCategories(cats: List<Category>) {
        _categories.value = cats
        showSync { _user.value?.uid?.let { repo.saveCategories(it, cats) } }
    }

    fun addCategory(cat: Category) = saveCategories(_categories.value + cat)

    fun addWrong(id: String) {
        viewModelScope.launch {
            val n = _stats.value.toMutableMap().also { it[id] = (it[id] ?: 0) + 1 }
            _stats.value = n
            _user.value?.uid?.let { repo.saveStats(it, n) }
        }
    }

    fun clearStats() {
        viewModelScope.launch {
            _stats.value = emptyMap()
            _user.value?.uid?.let { repo.saveStats(it, emptyMap()) }
        }
    }

    // ── 이미지 업로드 (Cloudinary) ──
    fun uploadImage(boneId: String, uri: Uri) {
        val uid = _user.value?.uid ?: return
        viewModelScope.launch {
            _uploadingIds.value = _uploadingIds.value + boneId
            val url = repo.uploadImage(context, uid, boneId, uri)
            if (url != null) {
                _imageUrls.value = _imageUrls.value + (boneId to url)
                repo.saveImageUrl(uid, boneId, url)
            }
            _uploadingIds.value = _uploadingIds.value - boneId
        }
    }

    // ── 이미지 삭제 (Firestore URL 제거만, Cloudinary 파일은 유지) ──
    fun deleteImage(boneId: String) {
        val uid = _user.value?.uid ?: return
        viewModelScope.launch {
            repo.removeImageUrl(uid, boneId)
            _imageUrls.value = _imageUrls.value - boneId
        }
    }

    fun signOut() { viewModelScope.launch { repo.signOut() } }

    override fun onCleared() { super.onCleared(); repo.removeAuthStateListener(authListener) }
}
