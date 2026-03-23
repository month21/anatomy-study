package com.month21.anatomy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.month21.anatomy.data.*
import com.month21.anatomy.ui.components.*

@Composable
fun CategoryManagerScreen(
    categories: List<Category>,
    bones: List<Bone>,
    onSave: (List<Category>) -> Unit
) {
    var cats by remember(categories) { mutableStateOf(categories.sorted()) }
    var editCat by remember { mutableStateOf<Category?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var confirmName by remember { mutableStateOf<String?>(null) }

    fun move(i: Int, up: Boolean) {
        val list = cats.toMutableList()
        val j = if (up) i - 1 else i + 1
        if (j < 0 || j >= list.size) return
        val tmp = list[i]; list[i] = list[j]; list[j] = tmp
        cats = list.mapIndexed { idx, c -> c.copy(order = idx) }
        onSave(cats)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("분류 관리", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Button(onClick = { showAdd = true }, shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))) {
                Text("+ 추가", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Text("탭해서 색상·이름 수정 / 화살표로 순서 변경", fontSize = 12.sp, color = Color(0xFF9CA3AF))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cats.forEachIndexed { i, cat ->
                val catColor = cat.color()
                val boneCount = bones.count { it.cat == cat.name }

                Card(
                    onClick = { editCat = cat },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, catColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 색상 원
                        Box(
                            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(catColor),
                            contentAlignment = Alignment.Center
                        ) { Text(cat.name.take(1), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                        // 이름 + 정보
                        Column(Modifier.weight(1f)) {
                            Text(cat.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (cat.axial) "축뼈대" else "팔다리뼈대", fontSize = 11.sp, color = Color(0xFF6B7280))
                                Text("·", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                Text("${boneCount}개 뼈", fontSize = 11.sp, color = Color(0xFF6B7280))
                            }
                        }

                        // 순서 버튼
                        Column {
                            IconButton(onClick = { move(i, true) }, enabled = i > 0,
                                modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "위로",
                                    modifier = Modifier.size(18.dp),
                                    tint = if (i > 0) Color(0xFF374151) else Color(0xFFD1D5DB))
                            }
                            IconButton(onClick = { move(i, false) }, enabled = i < cats.lastIndex,
                                modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "아래로",
                                    modifier = Modifier.size(18.dp),
                                    tint = if (i < cats.lastIndex) Color(0xFF374151) else Color(0xFFD1D5DB))
                            }
                        }

                        // 삭제 버튼
                        OutlinedButton(
                            onClick = { confirmName = cat.name },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) { Text("삭제", fontSize = 12.sp) }
                    }
                }
            }
        }
    }

    // 수정 다이얼로그
    editCat?.let { c -> CategoryEditDialog(
        cat = c,
        onDismiss = { editCat = null },
        onConfirm = { updated ->
            cats = cats.map { if (it.name == c.name) updated else it }
            // 이름 변경 시 뼈 데이터도 같이 업데이트해야 하므로 여기선 name 변경 비허용
            onSave(cats)
            editCat = null
        }
    )}

    // 새 카테고리 추가 다이얼로그
    if (showAdd) {
        CategoryEditDialog(
            cat = Category(order = (cats.maxOfOrNull { it.order } ?: 0) + 1),
            isNew = true,
            onDismiss = { showAdd = false },
            onConfirm = { newCat ->
                cats = cats + newCat
                onSave(cats)
                showAdd = false
            }
        )
    }

    // 삭제 확인
    if (confirmName != null) {
        val cnt = bones.count { it.cat == confirmName }
        AlertDialog(
            onDismissRequest = { confirmName = null },
            icon  = { Text("🗑️", fontSize = 28.sp) },
            title = { Text("'$confirmName' 삭제?", fontWeight = FontWeight.ExtraBold) },
            text  = { Text(if (cnt > 0) "이 분류에 속한 단어 ${cnt}개도 이 분류에서 빠져요." else "이 분류를 삭제할까요?", color = Color(0xFF6B7280)) },
            confirmButton = {
                Button(onClick = {
                    cats = cats.filter { it.name != confirmName }
                    onSave(cats)
                    confirmName = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                    Text("삭제", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { OutlinedButton(onClick = { confirmName = null }) { Text("취소") } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ── 카테고리 편집 다이얼로그 ──
@Composable
fun CategoryEditDialog(
    cat: Category,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit
) {
    var name     by remember { mutableStateOf(cat.name) }
    var colorHex by remember { mutableStateOf(cat.colorHex.ifBlank { COLOR_PALETTE[0] }) }
    var axial    by remember { mutableStateOf(cat.axial) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(if (isNew) "새 분류 추가" else "분류 수정", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)

                // 이름
                FieldLabel("이름 *")
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB)),
                    readOnly = !isNew  // 기존 카테고리는 이름 변경 불가 (뼈 데이터 연동 문제)
                )
                if (!isNew) Text("※ 이름은 변경할 수 없어요 (단어 데이터와 연동됨)", fontSize = 11.sp, color = Color(0xFF9CA3AF))

                // 색상 팔레트
                FieldLabel("색상")
                val previewColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(previewColor))
                    Text(colorHex, fontSize = 12.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold)
                }
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(COLOR_PALETTE.size) { i ->
                        val hex = COLOR_PALETTE[i]
                        val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                        Box(
                            modifier = Modifier
                                .size(32.dp).clip(RoundedCornerShape(8.dp)).background(c)
                                .then(if (colorHex == hex) Modifier.border(2.5.dp, Color(0xFF111827), RoundedCornerShape(8.dp)) else Modifier)
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorHex == hex) Text("✓", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 유형
                FieldLabel("유형")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true to "축뼈대", false to "팔다리뼈대").forEach { (v, label) ->
                        FilterChip(selected = axial == v, onClick = { axial = v },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF111827), selectedLabelColor = Color.White))
                    }
                }

                // 버튼
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Text("취소") }
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(cat.copy(name = name.trim(), colorHex = colorHex, axial = axial)) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(2f), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
                    ) { Text("저장", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
