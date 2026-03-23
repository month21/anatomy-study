package com.month21.anatomy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.month21.anatomy.ui.*
import com.month21.anatomy.ui.theme.AnatomyTheme
import com.month21.anatomy.viewmodel.MainViewModel
import com.month21.anatomy.viewmodel.SyncStatus
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    private lateinit var vm: MainViewModel

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        signInLauncher.launch(GoogleSignIn.getClient(this, gso).signInIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnatomyTheme {
                val viewModel: MainViewModel = viewModel()
                vm = viewModel
                AnatomyApp(vm = viewModel, onSignIn = { launchGoogleSignIn() })
            }
        }
    }
}

data class NavItem(val id: String, val label: String, val icon: ImageVector, val badge: Int? = null)

@Composable
fun AnatomyApp(vm: MainViewModel, onSignIn: () -> Unit) {
    val user        by vm.user.collectAsState()
    val bones       by vm.bones.collectAsState()
    val stats       by vm.stats.collectAsState()
    val categories  by vm.categories.collectAsState()
    val loading     by vm.loading.collectAsState()
    val syncStatus  by vm.sync.collectAsState()
    var currentTab  by remember { mutableStateOf("classify") }
    var showProfile by remember { mutableStateOf(false) }

    val wrongTotal = stats.values.sum()
    val navItems = listOf(
        NavItem("classify",  "분류",  Icons.Default.GridView),
        NavItem("compare",   "비교",  Icons.Default.Compare),
        NavItem("flashcard", "카드",  Icons.Default.Style),
        NavItem("quiz",      "퀴즈",  Icons.Default.Quiz),
        NavItem("stats",     "통계",  Icons.Default.BarChart, if (wrongTotal > 0) wrongTotal else null),
        NavItem("manage",    "단어",  Icons.Default.Edit),
        NavItem("category",  "분류관리", Icons.Default.Category),
    )

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🦴", fontSize = 40.sp)
                CircularProgressIndicator(color = Color(0xFF111827))
                Text("계정 데이터 불러오는 중...", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        return
    }

    Scaffold(
        topBar = {
            Column {
                Surface(shadowElevation = 2.dp, color = Color.White) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("🦴 골격계 해부학", fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("SKELETAL ANATOMY · ${bones.size}개 단어", fontSize = 10.sp, color = Color(0xFF9CA3AF), letterSpacing = 1.5.sp)
                                when (syncStatus) {
                                    SyncStatus.SAVING -> Text("저장 중...", fontSize = 10.sp, color = Color(0xFF0EA5E9))
                                    SyncStatus.SAVED  -> Text("✓ 저장됨",   fontSize = 10.sp, color = Color(0xFF16A34A))
                                    else -> {}
                                }
                            }
                        }
                        if (user != null) {
                            IconButton(onClick = { showProfile = true }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "프로필", tint = Color(0xFF374151))
                            }
                        } else {
                            OutlinedButton(onClick = onSignIn, shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                                Text("Google 로그인", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (user == null) {
                    Surface(color = Color(0xFFFEFCE8)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("🔑", fontSize = 13.sp); Spacer(Modifier.width(8.dp))
                            Text("Google 로그인하면 어느 기기에서나 데이터가 저장돼요.", fontSize = 12.sp, color = Color(0xFF92400E), modifier = Modifier.weight(1f))
                            TextButton(onClick = onSignIn) { Text("로그인 →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706)) }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentTab == item.id,
                        onClick  = { currentTab = item.id },
                        icon = {
                            BadgedBox(badge = {
                                if (item.badge != null) Badge { Text(if (item.badge > 99) "99+" else "${item.badge}", fontSize = 8.sp) }
                            }) { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp)) }
                        },
                        label  = { Text(item.label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = Color(0xFF111827), selectedTextColor   = Color(0xFF111827),
                            unselectedIconColor = Color(0xFF9CA3AF), unselectedTextColor = Color(0xFF9CA3AF),
                            indicatorColor      = Color(0xFFF3F4F6)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (currentTab) {
                "classify"  -> ClassifyScreen(bones, categories, vm::saveBones, vm::addCategory)
                "compare"   -> CompareScreen(bones)
                "flashcard" -> {
                    val imageUrls   by vm.imageUrls.collectAsState()
                    val uploadingIds by vm.uploadingIds.collectAsState()
                    FlashcardScreen(
                        bones        = bones,
                        imageUrls    = imageUrls,
                        uploadingIds = uploadingIds,
                        onUploadImage = vm::uploadImage,
                        onDeleteImage = vm::deleteImage
                    )
                }
                "quiz"      -> QuizScreen(bones, vm::addWrong)
                "stats"     -> StatsScreen(bones, stats, vm::clearStats, categories)
                "manage"    -> WordManagerScreen(bones, categories, vm::saveBones, vm::addCategory)
                "category"  -> CategoryManagerScreen(categories, bones, vm::saveCategories)
            }
        }
    }

    if (showProfile && user != null) {
        AlertDialog(
            onDismissRequest = { showProfile = false },
            icon  = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color(0xFF6366F1)) },
            title = { Text(user!!.displayName ?: "사용자", fontWeight = FontWeight.ExtraBold) },
            text  = { Text(user!!.email ?: "", color = Color(0xFF6B7280)) },
            confirmButton = {
                Button(onClick = { vm.signOut(); showProfile = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                    Text("로그아웃", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { OutlinedButton(onClick = { showProfile = false }) { Text("닫기") } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
