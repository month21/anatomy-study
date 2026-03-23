package com.month21.anatomy.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.month21.anatomy.data.*
import com.month21.anatomy.ui.components.*

@Composable
fun ClassifyScreen(
    bones: List<Bone>,
    categories: List<Category>,
    onSaveBones: (List<Bone>) -> Unit,
    onAddCategory: (Category) -> Unit
) {
    var detailBone    by remember { mutableStateOf<Bone?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // 아코디언 상태 — 기본 닫힘
    var axialOpen by remember { mutableStateOf(false) }
    var appOpen   by remember { mutableStateOf(false) }

    val sortedCats  = categories.sorted()
    val axialCats   = sortedCats.filter { it.axial }
    val appCats     = sortedCats.filter { !it.axial }
    val catColorMap = categories.colorMap()

    val axialBones = bones.filter { b -> axialCats.any { it.name == b.cat } }
    val appBones   = bones.filter { b -> appCats.any  { it.name == b.cat } }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── 축뼈대 아코디언 ──
            AccordionSection(
                title      = "축뼈대",
                count      = axialBones.size,
                catCount   = axialCats.size,
                accent     = Color(0xFF0EA5E9),
                isOpen     = axialOpen,
                onToggle   = { axialOpen = !axialOpen }
            ) {
                axialCats.forEach { cat ->
                    val group = bones.filter { it.cat == cat.name }
                    if (group.isEmpty()) return@forEach
                    val catColor = catColorMap[cat.name] ?: Color.Gray
                    CatGroup(cat.name, catColor, group) { detailBone = it }
                }
            }

            Divider(color = Color(0xFFF3F4F6), thickness = 6.dp)

            // ── 팔다리뼈대 아코디언 ──
            AccordionSection(
                title      = "팔다리뼈대",
                count      = appBones.size,
                catCount   = appCats.size,
                accent     = Color(0xFFF97316),
                isOpen     = appOpen,
                onToggle   = { appOpen = !appOpen }
            ) {
                appCats.forEach { cat ->
                    val group = bones.filter { it.cat == cat.name }
                    if (group.isEmpty()) return@forEach
                    val catColor = catColorMap[cat.name] ?: Color.Gray
                    CatGroup(cat.name, catColor, group) { detailBone = it }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = Color(0xFF111827), contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("단어 추가", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    detailBone?.let {
        BoneDetailDialog(bone = it, categories = categories, onDismiss = { detailBone = null })
    }

    if (showAddDialog) {
        BoneFormDialog(
            title = "➕ 새 단어", categories = categories,
            onDismiss = { showAddDialog = false },
            onNewCategory = onAddCategory,
            onConfirm = { form ->
                val cat = categories.find { it.name == form.cat }
                onSaveBones(bones + Bone(
                    id = "c_${System.currentTimeMillis()}",
                    k = form.k, l = form.l, axial = cat?.axial ?: true,
                    cat = form.cat, desc = form.desc, tip = form.tip, story = form.story
                ))
                showAddDialog = false
            }
        )
    }
}

// ── 아코디언 섹션 컴포넌트 ──
@Composable
fun AccordionSection(
    title: String,
    count: Int,
    catCount: Int,
    accent: Color,
    isOpen: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 왼쪽 컬러 바
            Box(Modifier.width(4.dp).height(38.dp).clip(RoundedCornerShape(2.dp)).background(accent))

            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = accent)
                Text("${count}개 뼈 · ${catCount}개 카테고리", fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }

            // 화살표 (열리면 180도 회전)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(if (isOpen) 180f else 0f),
                tint = Color(0xFF9CA3AF)
            )
        }

        Divider(color = Color(0xFFF3F4F6))

        // 바디 (애니메이션)
        AnimatedVisibility(
            visible = isOpen,
            enter = expandVertically(tween(250)) + fadeIn(tween(200)),
            exit  = shrinkVertically(tween(200)) + fadeOut(tween(150))
        ) {
            Column { content() }
        }
    }
}

// ── 카테고리 그룹 ──
@Composable
fun CatGroup(
    catName: String,
    catColor: Color,
    bones: List<Bone>,
    onBoneClick: (Bone) -> Unit
) {
    Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
        // 카테고리 레이블
        Row(
            Modifier.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                catName.uppercase(),
                fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = catColor, letterSpacing = 1.5.sp
            )
            Divider(color = Color(0xFFF3F4F6), modifier = Modifier.weight(1f))
        }

        bones.forEach { bone ->
            Card(
                onClick = { onBoneClick(bone) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = catColor.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, catColor.copy(alpha = 0.16f))
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(catColor))
                    Text(bone.k, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937), modifier = Modifier.weight(1f))
                    Text(bone.l.split("(")[0].trim(),
                        fontSize = 10.sp, color = Color(0xFF9CA3AF),
                        fontStyle = FontStyle.Italic,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}
