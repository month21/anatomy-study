import com.month21.anatomy.data.colorMap
package com.month21.anatomy.ui

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
import com.month21.anatomy.data.*
import com.month21.anatomy.ui.components.*

@Composable
fun StatsScreen(bones: List<Bone>, stats: Map<String, Int>, onClear: () -> Unit, categories: List<com.month21.anatomy.data.Category> = emptyList()) {
    val catColorMap = categories.colorMap()
    var popup by remember { mutableStateOf<Bone?>(null) }
    val entries = stats.entries
        .mapNotNull { (id, cnt) -> bones.find { it.id == id }?.let { it to cnt } }
        .sortedByDescending { it.second }
    val total  = entries.sumOf { it.second }
    val maxCnt = entries.firstOrNull()?.second ?: 1

    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎉", fontSize = 40.sp)
                Text("아직 틀린 문제가 없어요!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text("퀴즈를 풀면 통계가 쌓여요.", fontSize = 14.sp, color = Color(0xFF9CA3AF))
            }
        }
        return
    }

    val catMap = mutableMapOf<String, Pair<Int, Int>>()
    entries.forEach { (bone, cnt) ->
        val prev = catMap[bone.cat] ?: (0 to 0)
        catMap[bone.cat] = (prev.first + cnt) to (prev.second + 1)
    }
    val catList = catMap.entries.sortedByDescending { it.value.first }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 요약 카드 3개
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple("총 오답",  "$total",                  Color(0xFFEF4444)),
                Triple("취약 단어", "${entries.size}개",       Color(0xFFF97316)),
                Triple("최다 오답", entries.first().first.k,  Color(0xFF6366F1))
            ).forEach { (label, value, color) ->
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.13f))) {
                    Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, fontSize = 10.sp, color = Color(0xFF9CA3AF))
                        Spacer(Modifier.height(4.dp))
                        Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1)
                    }
                }
            }
        }

        // 취약 카테고리
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📊 취약 카테고리", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            catList.forEach { (cat, pair) ->
                val (cnt, n) = pair
                val catColor = CAT_COLORS[cat] ?: Color.Gray
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(catColor))
                    Text(cat, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), modifier = Modifier.width(52.dp))
                    Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F4F6))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(cnt.toFloat() / total).clip(RoundedCornerShape(4.dp)).background(catColor))
                    }
                    Text("${cnt}회/${n}개", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                }
            }
        }

        // 틀린 단어 순위
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🔴 자주 틀리는 단어", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            entries.forEachIndexed { i, (bone, cnt) ->
                Card(onClick = { popup = bone }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(when (i) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "${i+1}" },
                            fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF9CA3AF), modifier = Modifier.width(22.dp))
                        Column(Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(bone.k, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                BoneTag(bone.cat, catColorMap[bone.cat] ?: Color.Gray, small = true)
                            }
                            Text(bone.l, fontSize = 11.sp, color = Color(0xFF9CA3AF), fontStyle = FontStyle.Italic)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${cnt}회", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                            Spacer(Modifier.height(3.dp))
                            Box(Modifier.width(56.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF3F4F6))) {
                                Box(Modifier.fillMaxHeight().fillMaxWidth(cnt.toFloat() / maxCnt)
                                    .clip(RoundedCornerShape(2.dp)).background(Color(0xFFEF4444)))
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onClear, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
        ) { Text("🗑️ 통계 초기화", fontWeight = FontWeight.SemiBold) }
    }

    popup?.let { BoneDetailDialog(bone = it, categories = emptyList(), onDismiss = { popup = null }) }
}
