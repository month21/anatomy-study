package com.month21.anatomy.data

import androidx.compose.ui.graphics.Color

// ── 카테고리 모델 ──
data class Category(
    val name: String = "",
    val colorHex: String = "#6366F1",
    val axial: Boolean = true,
    val order: Int = 0
) {
    fun color(): Color = try {
        Color(android.graphics.Color.parseColor(
            if (colorHex.startsWith("#")) colorHex else "#$colorHex"
        ))
    } catch (e: Exception) { Color(0xFF6366F1) }
}

// ── 색상 팔레트 16종 ──
val COLOR_PALETTE = listOf(
    "#6366F1", "#0EA5E9", "#8B5CF6", "#F59E0B",
    "#F97316", "#EC4899", "#EF4444", "#10B981",
    "#14B8A6", "#F43F5E", "#84CC16", "#06B6D4",
    "#A855F7", "#FB923C", "#22C55E", "#64748B",
)

// ── 기본 카테고리 ──
val DEFAULT_CATEGORIES = listOf(
    Category("두개골", "#6366F1", axial = true,  order = 0),
    Category("척주",   "#0EA5E9", axial = true,  order = 1),
    Category("흉곽",   "#8B5CF6", axial = true,  order = 2),
    Category("상지대",  "#F59E0B", axial = false, order = 3),
    Category("상지",   "#F97316", axial = false, order = 4),
    Category("하지대",  "#EC4899", axial = false, order = 5),
    Category("하지",   "#EF4444", axial = false, order = 6),
)

fun List<Category>.colorMap(): Map<String, Color>   = associate { it.name to it.color() }
fun List<Category>.axialMap(): Map<String, Boolean> = associate { it.name to it.axial }
fun List<Category>.sorted(): List<Category>         = sortedBy { it.order }

// ── 뼈 모델 ──
data class Bone(
    val id: String = "",
    val k: String = "",
    val l: String = "",
    val axial: Boolean = true,
    val cat: String = "",
    val desc: String = "",
    val tip: String = "",
    val story: String = ""
)

data class PairItem(val a: String, val b: String, val note: String)

val PAIRS = listOf(
    PairItem("radius",   "ulna",        "아래팔 두 뼈 — 위치로 구분"),
    PairItem("tibia",    "fibula",      "하퇴 두 뼈 — 굵기·위치로 구분"),
    PairItem("carpals",  "metacarpals", "손목 vs 손바닥"),
    PairItem("tarsals",  "metatarsals", "발목 vs 발바닥"),
    PairItem("cranium",  "mandible",    "두개골 — 고정 vs 움직임"),
    PairItem("ilium",    "ischium",     "골반 — 위 vs 뒤아래"),
    PairItem("ischium",  "pubis",       "골반 — 앉을 때 vs 앞쪽"),
    PairItem("cervical", "thoracic",    "척추 — 목 vs 등"),
    PairItem("thoracic", "lumbar",      "척추 — 등 vs 허리"),
)

val CAT_COLORS: Map<String, Color>   get() = DEFAULT_CATEGORIES.colorMap()
val CAT_AXIAL: Map<String, Boolean>  get() = DEFAULT_CATEGORIES.axialMap()
val ALL_CATS: List<String>           get() = DEFAULT_CATEGORIES.sorted().map { it.name }

val DEFAULT_BONES = listOf(
    Bone("cranium",     "두개골",   "Cranium",           true,  "두개골", "뇌를 보호하는 8개의 뼈로 구성",                         "전두·정·측측·후·접·사 = 8개!",          "두개(頭蓋) = 머리 뚜껑. 뇌라는 귀한 물건을 담은 딱딱한 뚜껑 상자를 상상해봐!"),
    Bone("mandible",    "하악골",   "Mandible",          true,  "두개골", "아래턱뼈. 얼굴뼈 중 유일하게 움직이는 뼈",             "하(下)=아래! 유일하게 움직이는 얼굴뼈",  "하악(下顎) = 아래 턱. '하악하악' 씹을 때 움직이는 유일한 얼굴뼈!"),
    Bone("cervical",    "경추",     "Cervical (C1~C7)",  true,  "척주",  "목뼈 7개. C1=환추, C2=축추",                          "경=목, 7개. C1환추·C2축추 필수!",        "경(頸)=목. 기린 목처럼 긴 목을 7개 뼈가 지탱. C1은 고리(환추), C2는 축처럼 회전!"),
    Bone("thoracic",    "흉추",     "Thoracic (T1~T12)", true,  "척주",  "등뼈 12개. 늑골과 관절",                             "흉=가슴, 12개 = 늑골 12쌍과 일치!",     "흉(胸)=가슴. 갈비뼈 12쌍이 모두 흉추에 붙어 있어."),
    Bone("lumbar",      "요추",     "Lumbar (L1~L5)",    true,  "척주",  "허리뼈 5개. 체중 부하 최대",                          "요=허리, 5개. 디스크 호발 부위",         "요(腰)=허리. 5개뿐이지만 온몸 체중을 받는 장사."),
    Bone("sacrum",      "천골",     "Sacrum",            true,  "척주",  "5개 천추 융합. 골반 후벽 형성",                       "5개 융합 = 역삼각형",                   "천(薦) = 신성하다. 5개 뼈가 하나로 합쳐진 역삼각형 쐐기돌."),
    Bone("coccyx",      "미골",     "Coccyx",            true,  "척주",  "꼬리뼈. 3~4개 미추 융합",                            "미=꼬리. 퇴화된 꼬리의 흔적!",           "미(尾)=꼬리. 인간이 원숭이였을 때의 흔적!"),
    Bone("sternum",     "흉골",     "Sternum",           true,  "흉곽",  "복장뼈. 흉골병+흉골체+검상돌기. CPR 부위",             "CPR 할 때 누르는 뼈! 병·체·검상",        "CPR 할 때 두 손으로 힘차게 누르는 바로 그 뼈!"),
    Bone("ribs",        "늑골",     "Ribs (12쌍)",       true,  "흉곽",  "갈비뼈 12쌍. 진늑골(1~7), 가늑골(8~10), 부유늑골(11~12)", "진(7)+가(3)+부유(2) = 12쌍!", "진짜(1~7)는 흉골에 직접 붙고, 가짜(8~10)는 연골로 연결!"),
    Bone("clavicle",    "쇄골",     "Clavicle",          false, "상지대", "빗장뼈. S자형. 낙상 시 골절 多",                      "S자 모양! 손 짚고 넘어지면 → 쇄골 골절", "쇄(鎖)=자물쇠. S자 모양의 빗장처럼 어깨를 잠근다."),
    Bone("scapula",     "견갑골",   "Scapula",           false, "상지대", "어깨뼈. 삼각형. 17개 근육 부착",                      "삼각형! 17개 근육의 집합소",             "견갑(肩胛)=어깨 등판. 날개뼈라고도 불러."),
    Bone("humerus",     "상완골",   "Humerus",           false, "상지",  "위팔뼈. 팔에서 가장 큰 뼈",                          "상(上)완=위팔. 팔에서 최장!",            "상완(上腕)=위팔. 팔 뼈 중 가장 길고 굵어!"),
    Bone("radius",      "요골",     "Radius",            false, "상지",  "노뼈. 아래팔 엄지쪽(외측). 콜레스 골절 多",            "요=엄지쪽! 콜레스 골절 부위",            "요(橈)=노. 배의 노처럼 팔 회전의 중심!"),
    Bone("ulna",        "척골",     "Ulna",              false, "상지",  "자뼈. 아래팔 새끼손가락쪽. 주두 형성",                 "척=새끼쪽! 주두=팔꿈치 뾰족한 부분",     "척(尺)=자(ruler). 새끼손가락 쪽의 길쭉한 뼈."),
    Bone("carpals",     "수근골",   "Carpals (8개)",     false, "상지",  "손목뼈 8개. 근위열 4개 + 원위열 4개",                 "주월삼두 / 대소두유 = 8개!",             "손목을 꺾을 때 느껴지는 작은 뼈들이 8개!"),
    Bone("metacarpals", "중수골",   "Metacarpals (5개)", false, "상지",  "손허리뼈 5개. 손바닥 형성",                          "손바닥의 5개 뼈!",                      "손바닥을 펼치면 보이는 5개의 뼈대."),
    Bone("phalanges_h", "지골(손)", "Phalanges (손)",    false, "상지",  "손가락뼈 14개. 엄지=2마디, 나머지=3마디",             "엄지 2+나머지 4×3=14개!",               "엄지는 2마디, 나머지 손가락은 3마디."),
    Bone("ilium",       "장골",     "Ilium",             false, "하지대", "엉덩뼈. 골반 최대·최광. ASIS 촉지 가능",             "장=크고 넓다! ASIS 직접 만져보기",       "장(腸)=창자 옆 뼈. 골반에서 가장 넓고 큰 날개 모양."),
    Bone("ischium",     "좌골",     "Ischium",           false, "하지대", "궁둥뼈. 앉을 때 체중이 실리는 뼈",                    "좌(坐)=앉다! 의자에 닿는 뼈",            "좌(坐)=앉다! 의자에 앉으면 딱딱하게 닿는 그 부분이 바로 좌골."),
    Bone("pubis",       "치골",     "Pubis",             false, "하지대", "두덩뼈. 골반 전면. 분만 시 이완",                     "골반 앞 연결! 분만 시 산도 확장",         "골반 앞쪽 가운데를 연결하는 뼈."),
    Bone("femur",       "대퇴골",   "Femur",             false, "하지",  "넓적다리뼈. 인체 최장·최강",                         "인체 최장·최강! 체중의 5배 견딤",         "인체에서 가장 길고 강한 뼈!"),
    Bone("patella",     "슬개골",   "Patella",           false, "하지",  "무릎뼈. 인체 최대 종자골",                           "인체 최대 종자골! 힘줄 속에 박힘",        "슬(膝)=무릎. 씨앗처럼 힘줄 속에 박혀있는 뼈."),
    Bone("tibia",       "경골",     "Tibia",             false, "하지",  "정강뼈. 하퇴 내측. 체중 지탱 주역",                   "내측+체중 지탱! 앞에서 만져짐",           "경(脛)=정강이. 하퇴의 굵고 내측에 있는 주역!"),
    Bone("fibula",      "비골",     "Fibula",            false, "하지",  "종아리뼈. 하퇴 외측의 가는 뼈",                      "외측+가는 뼈! 발목 삐면 → 비골 골절",    "비(腓)=종아리. 경골 옆에서 보조하는 가는 뼈."),
    Bone("tarsals",     "족근골",   "Tarsals (7개)",     false, "하지",  "발목뼈 7개. 거골·종골·주상골 등",                    "거·종·주·입·설설설 = 7개!",              "발목을 이루는 7개 블록!"),
    Bone("metatarsals", "중족골",   "Metatarsals (5개)", false, "하지",  "발허리뼈 5개. 발바닥 형성",                          "발바닥 5개 뼈!",                        "발바닥을 이루는 5개의 뼈."),
    Bone("phalanges_f", "지골(발)", "Phalanges (발)",    false, "하지",  "발가락뼈 14개. 보행에 특화",                         "손과 동일 14개!",                       "손가락과 똑같이 14개!"),
)
