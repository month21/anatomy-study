package com.month21.anatomy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.month21.anatomy.data.*
import com.month21.anatomy.ui.components.*

@Composable
fun WordManagerScreen(
    bones: List<Bone>,
    categories: List<Category>,
    onSaveBones: (List<Bone>) -> Unit,
    onAddCategory: (Category) -> Unit
) {
    var search    by remember { mutableStateOf("") }
    var editBone  by remember { mutableStateOf<Bone?>(null) }
    var showAdd   by remember { mutableStateOf(false) }
    var confirmId by remember { mutableStateOf<String?>(null) }

    val catColorMap = categories.colorMap()

    val filtered = bones.filter {
        it.k.contains(search) || it.l.lowercase().contains(search.lowercase()) || it.cat.contains(search)
    }

    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("🔍 검색", fontSize = 13.sp) },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
            )
            Button(onClick = { showAdd = true }, shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))) {
                Text("+ 추가", fontWeight = FontWeight.Bold)
            }
        }

        Text("${filtered.size}개", fontSize = 12.sp, color = Color(0xFF9CA3AF))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filtered.forEach { b ->
                val catColor = catColorMap[b.cat] ?: Color.Gray
                val isCustom = DEFAULT_BONES.none { it.id == b.id }
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(catColor))
                        Column(Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(b.k, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                BoneTag(b.cat, catColor, small = true)
                                if (isCustom) BoneTag("추가됨", Color(0xFF8B5CF6), small = true)
                            }
                            Text(b.l, fontSize = 11.sp, color = Color(0xFF9CA3AF), fontStyle = FontStyle.Italic)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { editBone = b }, shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                                Text("수정", fontSize = 12.sp)
                            }
                            OutlinedButton(onClick = { confirmId = b.id }, shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))) {
                                Text("삭제", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { confirmId = "reset" }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) { Text("🔄 기본값 초기화", fontWeight = FontWeight.SemiBold) }
        }
    }

    if (showAdd) {
        BoneFormDialog(
            title = "➕ 새 단어", categories = categories,
            onDismiss = { showAdd = false },
            onNewCategory = onAddCategory,
            onConfirm = { form ->
                val cat = categories.find { it.name == form.cat }
                onSaveBones(bones + Bone(
                    id = "c_${System.currentTimeMillis()}", k = form.k, l = form.l,
                    axial = cat?.axial ?: true, cat = form.cat,
                    desc = form.desc, tip = form.tip, story = form.story
                ))
                showAdd = false
            }
        )
    }

    editBone?.let { b ->
        BoneFormDialog(
            title = "✏️ 수정",
            initialForm = BoneForm(b.k, b.l, b.cat, b.desc, b.tip, b.story),
            categories = categories,
            onDismiss = { editBone = null },
            onNewCategory = onAddCategory,
            onConfirm = { form ->
                val cat = categories.find { it.name == form.cat }
                onSaveBones(bones.map { if (it.id == b.id) it.copy(k=form.k, l=form.l, cat=form.cat,
                    desc=form.desc, tip=form.tip, story=form.story, axial=cat?.axial?:true) else it })
                editBone = null
            }
        )
    }

    if (confirmId != null) {
        AlertDialog(
            onDismissRequest = { confirmId = null },
            icon  = { Text(if (confirmId == "reset") "⚠️" else "🗑️", fontSize = 28.sp) },
            title = { Text(if (confirmId == "reset") "기본값으로 초기화?" else "삭제할까요?", fontWeight = FontWeight.ExtraBold) },
            text  = { Text(if (confirmId == "reset") "추가한 단어가 모두 사라져요." else "삭제 후 되돌릴 수 없어요.", color = Color(0xFF6B7280)) },
            confirmButton = {
                Button(onClick = {
                    if (confirmId == "reset") onSaveBones(DEFAULT_BONES)
                    else onSaveBones(bones.filter { it.id != confirmId })
                    confirmId = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                    Text("확인", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { OutlinedButton(onClick = { confirmId = null }) { Text("취소") } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
