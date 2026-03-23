package com.month21.anatomy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.month21.anatomy.data.*
import com.month21.anatomy.ui.components.*

@Composable
fun QuizScreen(bones: List<Bone>, onWrong: (String) -> Unit) {
    var tab by remember { mutableStateOf("choice") }
    Column(Modifier.fillMaxSize().padding(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            listOf("choice" to "📝 4지선다", "input" to "⌨️ 직접 입력").forEach { (t, label) ->
                Button(onClick = { tab = t }, shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tab == t) Color(0xFF111827) else Color(0xFFF3F4F6),
                        contentColor   = if (tab == t) Color.White else Color(0xFF6B7280)
                    )) { Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
        if (tab == "choice") ChoiceQuiz(bones, onWrong)
        else InputQuiz(bones, onWrong)
    }
}

@Composable
fun ChoiceQuiz(bones: List<Bone>, onWrong: (String) -> Unit) {
    if (bones.size < 4) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("단어가 4개 이상 필요합니다.", color = Color(0xFF9CA3AF)) }
        return
    }
    var correct by remember { mutableIntStateOf(0) }
    var total   by remember { mutableIntStateOf(0) }
    var dir     by remember { mutableStateOf("k2l") }
    var curr    by remember { mutableStateOf(bones.random()) }
    var opts    by remember { mutableStateOf(genOpts(curr, bones)) }
    var sel     by remember { mutableStateOf<Bone?>(null) }
    fun next()  { curr = bones.random(); opts = genOpts(curr, bones); sel = null }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("k2l" to "한→라틴", "l2k" to "라틴→한").forEach { (d, l) ->
                    FilterChip(selected = dir == d, onClick = { dir = d; next() }, label = { Text(l, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF111827), selectedLabelColor = Color.White))
                }
            }
            if (total > 0) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("✓$correct", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("✗${total - correct}", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${correct * 100 / total}%", color = Color(0xFF9CA3AF), fontSize = 13.sp)
            }
        }
        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BoneTag(curr.cat, CAT_COLORS[curr.cat] ?: Color.Gray)
                Text(if (dir == "k2l") curr.k else curr.l, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                    fontStyle = if (dir == "l2k") FontStyle.Italic else FontStyle.Normal, textAlign = TextAlign.Center)
                Text(if (dir == "k2l") "라틴어 명칭을 고르세요" else "한국어 명칭을 고르세요", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        opts.forEachIndexed { i, opt ->
            val isC = opt.id == curr.id; val isS = sel?.id == opt.id
            Card(
                onClick = { if (sel != null) return@Card; sel = opt; total++; if (isC) correct++ else onWrong(curr.id) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = when { sel == null -> Color.White; isC -> Color(0xFFF0FDF4); isS -> Color(0xFFFEF2F2); else -> Color(0xFFF9FAFB) }),
                border = BorderStroke(1.dp, when { sel == null -> Color(0xFFE5E7EB); isC -> Color(0xFF86EFAC); isS -> Color(0xFFFCA5A5); else -> Color(0xFFE5E7EB) })
            ) {
                Text("${"ABCD"[i]}. ${if (dir == "k2l") opt.l else opt.k}",
                    fontSize = if (dir == "l2k") 15.sp else 14.sp,
                    color = when { sel == null -> Color(0xFF111827); isC -> Color(0xFF166534); isS -> Color(0xFF991B1B); else -> Color(0xFF9CA3AF) },
                    fontStyle = if (dir == "l2k") FontStyle.Italic else FontStyle.Normal,
                    fontWeight = if (isS) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp))
            }
        }
        if (sel != null) {
            if (curr.tip.isNotEmpty())   InfoBox("💡 ${curr.tip}",   Color(0xFFFEFCE8), Color(0xFFFDE68A), Color(0xFF92400E))
            if (curr.story.isNotEmpty()) InfoBox("📖 ${curr.story}", Color(0xFFF0FDF4), Color(0xFFBBF7D0), Color(0xFF166534))
            Button(onClick = ::next, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))) {
                Text("다음 문제 →", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun InputQuiz(bones: List<Bone>, onWrong: (String) -> Unit) {
    var correct  by remember { mutableIntStateOf(0) }
    var total    by remember { mutableIntStateOf(0) }
    var dir      by remember { mutableStateOf("k2l") }
    var curr     by remember { mutableStateOf(bones.random()) }
    var input    by remember { mutableStateOf("") }
    var result   by remember { mutableStateOf<Boolean?>(null) }
    val focusReq = remember { FocusRequester() }

    fun next()  { curr = bones.random(); input = ""; result = null }
    fun check() {
        if (result != null || input.isBlank()) return
        val ans  = if (dir == "k2l") curr.l else curr.k
        val user = input.trim().lowercase()
        val ok   = user == ans.trim().lowercase() || user == ans.split("(")[0].trim().lowercase()
        result   = ok; total++
        if (ok) correct++ else onWrong(curr.id)
    }
    LaunchedEffect(curr) { try { focusReq.requestFocus() } catch (_: Exception) {} }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("k2l" to "한→라틴", "l2k" to "라틴→한").forEach { (d, l) ->
                    FilterChip(selected = dir == d, onClick = { dir = d; next() }, label = { Text(l, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF111827), selectedLabelColor = Color.White))
                }
            }
            if (total > 0) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("✓$correct", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("✗${total - correct}", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BoneTag(curr.cat, CAT_COLORS[curr.cat] ?: Color.Gray)
                Text(if (dir == "k2l") curr.k else curr.l, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                    fontStyle = if (dir == "l2k") FontStyle.Italic else FontStyle.Normal, textAlign = TextAlign.Center)
                Text(if (dir == "k2l") "라틴어로 직접 입력하세요" else "한국어로 직접 입력하세요", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        OutlinedTextField(
            value = input, onValueChange = { if (result == null) input = it },
            enabled = result == null, modifier = Modifier.fillMaxWidth().focusRequester(focusReq),
            placeholder = { Text(if (dir == "k2l") "라틴어 명칭 입력..." else "한국어 명칭 입력...") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = when (result) { true -> Color(0xFF86EFAC); false -> Color(0xFFFCA5A5); else -> Color(0xFF111827) },
                unfocusedBorderColor = when (result) { true -> Color(0xFF86EFAC); false -> Color(0xFFFCA5A5); else -> Color(0xFFE5E7EB) }
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (result != null) next() else check() }),
            trailingIcon    = { result?.let { Text(if (it) "✅" else "❌", fontSize = 18.sp) } }
        )
        if (result != null) {
            if (result == false) InfoBox("정답: ${if (dir == "k2l") curr.l else curr.k}", Color(0xFFFEF2F2), Color(0xFFFCA5A5), Color(0xFF991B1B))
            else Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF0FDF4)).border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp)).padding(12.dp), contentAlignment = Alignment.Center) {
                Text("🎉 정답!", fontSize = 14.sp, color = Color(0xFF166534), fontWeight = FontWeight.SemiBold)
            }
            if (curr.tip.isNotEmpty())   InfoBox("💡 ${curr.tip}",   Color(0xFFFEFCE8), Color(0xFFFDE68A), Color(0xFF92400E))
            if (curr.story.isNotEmpty()) InfoBox("📖 ${curr.story}", Color(0xFFF0FDF4), Color(0xFFBBF7D0), Color(0xFF166534))
            Button(onClick = ::next, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))) {
                Text("다음 문제 → (Enter)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }
        } else {
            Button(onClick = ::check, enabled = input.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (input.isNotBlank()) Color(0xFF111827) else Color(0xFFE5E7EB),
                    contentColor   = if (input.isNotBlank()) Color.White else Color(0xFF9CA3AF)
                )) { Text("확인 (Enter)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp)) }
        }
    }
}

private fun genOpts(curr: Bone, bones: List<Bone>): List<Bone> =
    (bones.filter { it.id != curr.id }.shuffled().take(3) + curr).shuffled()
