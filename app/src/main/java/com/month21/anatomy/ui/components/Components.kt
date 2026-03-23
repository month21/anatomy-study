package com.month21.anatomy.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.month21.anatomy.data.*

// ── BoneTag ──
@Composable
fun BoneTag(label: String, color: Color, small: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.27f), RoundedCornerShape(20.dp))
            .padding(horizontal = if (small) 8.dp else 10.dp, vertical = if (small) 2.dp else 3.dp)
    ) {
        Text(label, fontSize = if (small) 10.sp else 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ── InfoBox ──
@Composable
fun InfoBox(text: String, bg: Color, border: Color, textColor: Color) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) { Text(text, fontSize = 13.sp, color = textColor, lineHeight = 20.sp) }
}

// ── FieldLabel ──
@Composable
fun FieldLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 0.8.sp)
}

// ── Bone Detail Dialog ──
@Composable
fun BoneDetailDialog(bone: Bone, categories: List<Category>, onDismiss: () -> Unit) {
    val color = categories.find { it.name == bone.cat }?.color() ?: Color(0xFF6366F1)
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BoneTag(if (bone.axial) "축뼈대" else "팔다리뼈대", if (bone.axial) Color(0xFF0EA5E9) else Color(0xFFF97316))
                    BoneTag(bone.cat, color)
                }
                Text(bone.k, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(bone.l, fontSize = 13.sp, color = Color(0xFF9CA3AF), fontStyle = FontStyle.Italic)
                if (bone.desc.isNotEmpty()) Text(bone.desc, fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF374151))
                if (bone.tip.isNotEmpty())   InfoBox("💡 ${bone.tip}",   Color(0xFFFEFCE8), Color(0xFFFDE68A), Color(0xFF92400E))
                if (bone.story.isNotEmpty()) InfoBox("📖 ${bone.story}", Color(0xFFF0FDF4), Color(0xFFBBF7D0), Color(0xFF166534))
                Spacer(Modifier.height(4.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))) {
                    Text("닫기", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// ── BoneForm ──
data class BoneForm(
    val k: String = "", val l: String = "", val cat: String = "두개골",
    val desc: String = "", val tip: String = "", val story: String = ""
)

// ── BoneFormDialog — 새 카테고리 추가 + 기존 선택 지원 ──
@Composable
fun BoneFormDialog(
    title: String,
    initialForm: BoneForm = BoneForm(),
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (BoneForm) -> Unit,
    onNewCategory: ((Category) -> Unit)? = null   // 새 카테고리 저장 콜백
) {
    var form by remember { mutableStateOf(initialForm) }
    var showNewCatInput by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatColor by remember { mutableStateOf(COLOR_PALETTE[0]) }
    var newCatAxial by remember { mutableStateOf(true) }

    val sortedCats = categories.sorted()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)

                // 한국어 / 라틴어
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        FieldLabel("한국어 *")
                        OutlinedTextField(
                            value = form.k, onValueChange = { form = form.copy(k = it) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        FieldLabel("라틴어 *")
                        OutlinedTextField(
                            value = form.l, onValueChange = { form = form.copy(l = it) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
                        )
                    }
                }

                // ── 분류 선택 + 새 카테고리 추가 ──
                FieldLabel("분류")
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 기존 카테고리 칩
                    items(sortedCats.size) { i ->
                        val cat = sortedCats[i]
                        FilterChip(
                            selected = form.cat == cat.name,
                            onClick  = { form = form.copy(cat = cat.name); showNewCatInput = false },
                            label    = { Text(cat.name, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cat.color(),
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                    // + 새 카테고리 버튼
                    item {
                        FilterChip(
                            selected = showNewCatInput,
                            onClick  = { showNewCatInput = !showNewCatInput },
                            label    = { Text("+ 새 분류", fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF111827),
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }

                // 새 카테고리 입력 패널
                if (showNewCatInput) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("새 분류 만들기", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            // 이름 입력
                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                placeholder = { Text("분류 이름", fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp), singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
                            )

                            // 색상 팔레트
                            FieldLabel("색상")
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(COLOR_PALETTE.size) { i ->
                                    val hex = COLOR_PALETTE[i]
                                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(c)
                                            .then(
                                                if (newCatColor == hex)
                                                    Modifier.border(2.dp, Color(0xFF111827), RoundedCornerShape(8.dp))
                                                else Modifier
                                            )
                                            .clickable { newCatColor = hex }
                                    ) {
                                        if (newCatColor == hex) {
                                            Text("✓", fontSize = 14.sp, color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.Center))
                                        }
                                    }
                                }
                            }

                            // 축뼈대 / 팔다리뼈대 선택
                            FieldLabel("분류 유형")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(true to "축뼈대", false to "팔다리뼈대").forEach { (v, label) ->
                                    FilterChip(
                                        selected = newCatAxial == v,
                                        onClick  = { newCatAxial = v },
                                        label    = { Text(label, fontSize = 12.sp) },
                                        colors   = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF111827),
                                            selectedLabelColor     = Color.White
                                        )
                                    )
                                }
                            }

                            // 추가 버튼
                            Button(
                                onClick = {
                                    if (newCatName.isNotBlank()) {
                                        val newCat = Category(
                                            name     = newCatName.trim(),
                                            colorHex = newCatColor,
                                            axial    = newCatAxial,
                                            order    = (categories.maxOfOrNull { it.order } ?: 0) + 1
                                        )
                                        onNewCategory?.invoke(newCat)
                                        form = form.copy(cat = newCat.name)
                                        newCatName = ""; showNewCatInput = false
                                    }
                                },
                                enabled = newCatName.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
                            ) { Text("분류 추가", fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                // 설명
                FieldLabel("설명")
                OutlinedTextField(
                    value = form.desc, onValueChange = { form = form.copy(desc = it) },
                    modifier = Modifier.fillMaxWidth().height(70.dp), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
                )

                // 암기 TIP
                FieldLabel("암기 TIP")
                OutlinedTextField(
                    value = form.tip, onValueChange = { form = form.copy(tip = it) },
                    modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
                )

                // 연상 스토리
                FieldLabel("연상 스토리 📖")
                OutlinedTextField(
                    value = form.story, onValueChange = { form = form.copy(story = it) },
                    modifier = Modifier.fillMaxWidth().height(78.dp), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB))
                )

                // 저장/취소 버튼
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Text("취소")
                    }
                    Button(
                        onClick = { if (form.k.isNotBlank() && form.l.isNotBlank()) onConfirm(form) },
                        enabled = form.k.isNotBlank() && form.l.isNotBlank(),
                        modifier = Modifier.weight(2f), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
                    ) { Text("저장", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
