package com.month21.anatomy.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.month21.anatomy.data.*
import com.month21.anatomy.ui.components.*

// 이미지 표시 위치
enum class ImgPos { NONE, FRONT, BACK, BOTH }

@Composable
fun FlashcardScreen(
    bones: List<Bone>,
    imageUrls: Map<String, String>,      // boneId -> Storage URL
    uploadingIds: Set<String>,           // 업로드 중인 boneId
    onUploadImage: (boneId: String, uri: Uri) -> Unit,
    onDeleteImage: (boneId: String) -> Unit,
) {
    if (bones.isEmpty()) return

    val cards = remember(bones) { bones.shuffled() }
    var idx       by remember { mutableIntStateOf(0) }
    var flipped   by remember { mutableStateOf(false) }
    var dir       by remember { mutableStateOf("k2l") }
    var imgPos    by remember { mutableStateOf(ImgPos.FRONT) }
    var showSettings by remember { mutableStateOf(false) }

    val card     = cards[idx % cards.size]
    val catColor = CAT_COLORS[card.cat] ?: Color.Gray
    val imageUrl = imageUrls[card.id]
    val isUploading = card.id in uploadingIds

    // 갤러리 런처
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUploadImage(card.id, it) }
    }

    // 뒤집기 애니메이션
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing), label = "flip"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── 방향 선택 + 설정 버튼 ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("k2l" to "한 → 라틴", "l2k" to "라틴 → 한").forEach { (d, label) ->
                Button(
                    onClick = { dir = d; flipped = false },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (dir == d) Color(0xFF111827) else Color(0xFFF3F4F6),
                        contentColor   = if (dir == d) Color.White else Color(0xFF6B7280)
                    )
                ) { Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            }
            IconButton(
                onClick = { showSettings = !showSettings },
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showSettings) Color(0xFF111827) else Color(0xFFF3F4F6))
            ) {
                Icon(Icons.Default.Settings, contentDescription = "설정",
                    tint = if (showSettings) Color.White else Color(0xFF6B7280))
            }
        }

        // ── 설정 패널 ──
        if (showSettings) {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🖼 이미지 표시 위치", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151), letterSpacing = 0.8.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(ImgPos.NONE to "없음", ImgPos.FRONT to "앞면",
                               ImgPos.BACK to "뒷면", ImgPos.BOTH to "앞+뒤").forEach { (pos, label) ->
                            FilterChip(
                                selected = imgPos == pos,
                                onClick  = { imgPos = pos },
                                label    = { Text(label, fontSize = 12.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF111827),
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }
                    Text("선택한 면에 이미지가 표시돼요.",
                        fontSize = 11.sp, color = Color(0xFF9CA3AF))
                }
            }
        }

        // ── 진행 바 ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${(idx % cards.size) + 1} / ${cards.size}", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            if (imageUrl != null) Text("🖼 이미지 있음", fontSize = 12.sp, color = Color(0xFF0EA5E9))
        }
        LinearProgressIndicator(
            progress = { ((idx % cards.size) + 1f) / cards.size },
            modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF111827), trackColor = Color(0xFFF3F4F6)
        )

        // ── 플래시카드 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
                .clip(RoundedCornerShape(16.dp))
                .background(if (rotation > 90f) catColor.copy(0.06f) else Color(0xFFF9FAFB))
                .border(1.5.dp,
                    if (rotation > 90f) catColor.copy(0.34f) else Color(0xFFE5E7EB),
                    RoundedCornerShape(16.dp))
                .clickable { flipped = !flipped }
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // 앞면
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 앞면 이미지
                    if (imageUrl != null && (imgPos == ImgPos.FRONT || imgPos == ImgPos.BOTH)) {
                        CardImage(url = imageUrl)
                    }
                    BoneTag(card.cat, catColor)
                    Text(
                        if (dir == "k2l") card.k else card.l,
                        fontSize = if (dir == "k2l") 34.sp else 20.sp,
                        fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                        fontStyle = if (dir == "l2k") FontStyle.Italic else FontStyle.Normal
                    )
                    Text("탭하여 뒤집기", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            } else {
                // 뒷면 (미러)
                Column(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (imageUrl != null && (imgPos == ImgPos.BACK || imgPos == ImgPos.BOTH)) {
                        CardImage(url = imageUrl)
                    }
                    Text(if (dir == "k2l") card.k else card.l,
                        fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Text(
                        if (dir == "k2l") card.l else card.k,
                        fontSize = if (dir == "l2k") 30.sp else 18.sp,
                        fontWeight = FontWeight.ExtraBold, color = catColor,
                        fontStyle = if (dir == "k2l") FontStyle.Italic else FontStyle.Normal,
                        textAlign = TextAlign.Center
                    )
                    if (card.tip.isNotEmpty())   InfoBox("💡 ${card.tip}",   Color(0xFFFEFCE8), Color(0xFFFDE68A), Color(0xFF92400E))
                    if (card.story.isNotEmpty()) InfoBox("📖 ${card.story}", Color(0xFFF0FDF4), Color(0xFFBBF7D0), Color(0xFF166534))
                }
            }
        }

        // ── 이미지 버튼 (없음 모드 제외) ──
        if (imgPos != ImgPos.NONE) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    isUploading -> {
                        OutlinedButton(onClick = {}, enabled = false,
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF0EA5E9))
                            Spacer(Modifier.width(8.dp))
                            Text("업로드 중...", fontSize = 13.sp)
                        }
                    }
                    imageUrl != null -> {
                        OutlinedButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF0EA5E9)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0369A1))
                        ) { Text("🔄 이미지 교체", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }

                        OutlinedButton(
                            onClick = { onDeleteImage(card.id) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) { Text("🗑", fontSize = 16.sp) }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF0EA5E9)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0369A1))
                        ) { Text("📷 이미지 추가", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }

        // ── 이전 / 다음 ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { if (idx > 0) { idx--; flipped = false } },
                enabled = idx > 0, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
            ) { Text("← 이전", fontSize = 14.sp) }
            Button(
                onClick = { idx++; flipped = false },
                modifier = Modifier.weight(2f), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
            ) { Text("다음 →", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// ── 이미지 컴포넌트 (Coil) ──
@Composable
fun CardImage(url: String) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = "카드 이미지",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 140.dp)
            .clip(RoundedCornerShape(10.dp)),
        loading = {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF0EA5E9))
            }
        }
    )
}
