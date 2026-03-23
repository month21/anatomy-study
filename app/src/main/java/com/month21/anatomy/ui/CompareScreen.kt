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
fun CompareScreen(bones: List<Bone>) {
    val validPairs = PAIRS.filter { p -> bones.any { it.id == p.a } && bones.any { it.id == p.b } }
    if (validPairs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("비교할 쌍이 없습니다.", color = Color(0xFF9CA3AF))
        }
        return
    }
    var pairIdx by remember { mutableIntStateOf(0) }
    val pair = validPairs[pairIdx % validPairs.size]
    val bA   = bones.first { it.id == pair.a }
    val bB   = bones.first { it.id == pair.b }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(validPairs.size) { i ->
                val p  = validPairs[i]
                val a  = bones.firstOrNull { it.id == p.a } ?: return@items
                val b  = bones.firstOrNull { it.id == p.b } ?: return@items
                FilterChip(
                    selected = i == pairIdx % validPairs.size,
                    onClick  = { pairIdx = i },
                    label    = { Text("${a.k} vs ${b.k}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF111827), selectedLabelColor = Color.White
                    )
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F9FF))
                .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) { Text("⚡ ${pair.note}", fontSize = 13.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.SemiBold) }

        BoneCompareCard(bA)
        Text("vs", fontSize = 16.sp, color = Color(0xFFD1D5DB), modifier = Modifier.align(Alignment.CenterHorizontally))
        BoneCompareCard(bB)
    }
}

@Composable
fun BoneCompareCard(bone: Bone) {
    val color = CAT_COLORS[bone.cat] ?: Color.Gray
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.04f)),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BoneTag(bone.cat, color, small = true)
            Text(bone.k, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(bone.l, fontSize = 11.sp, color = Color(0xFF9CA3AF), fontStyle = FontStyle.Italic)
            Text(bone.desc, fontSize = 13.sp, color = Color(0xFF374151), lineHeight = 20.sp)
            if (bone.tip.isNotEmpty())   InfoBox("💡 ${bone.tip}",   Color(0xFFFEFCE8), Color(0xFFFDE68A), Color(0xFF92400E))
            if (bone.story.isNotEmpty()) InfoBox("📖 ${bone.story}", Color(0xFFF0FDF4), Color(0xFFBBF7D0), Color(0xFF166534))
        }
    }
}
