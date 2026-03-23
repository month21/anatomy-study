# 🦴 골격계 해부학 — Android Native App (v2)

패키지: `com.month21.anatomy`

## 🔥 Firebase 설정 (필수 2단계)

### 1. Firebase Console
1. [console.firebase.google.com](https://console.firebase.google.com) → 프로젝트 선택/생성
2. **Authentication** → Google 로그인 활성화
3. **Firestore Database** → 데이터베이스 만들기 (테스트 모드)
4. **프로젝트 설정** → Android 앱 추가
   - 패키지 이름: **`com.month21.anatomy`**
   - `google-services.json` 다운로드

### 2. 파일 2개 교체 + Storage 활성화
```
# ① google-services.json 교체
app/google-services.json  ← Firebase에서 다운로드한 파일로 교체

# ② 웹 클라이언트 ID 입력
app/src/main/res/values/strings.xml
  <string name="default_web_client_id">여기에_웹_클라이언트_ID</string>
```

### 2-1. 이미지 저장 — Cloudinary (신규, 무료 25GB)
별도 설정 없음. 코드에 이미 설정 완료:
- Cloud Name: `dd52lyfox`
- Upload Preset: `anatomy_preset`
웹 클라이언트 ID 위치: Firebase 콘솔 → 프로젝트 설정 → 일반 → 웹 API 키 (또는 OAuth 클라이언트)

---

## 🛠 Android Studio 실행

```bash
1. Android Studio → Open → anatomy-app-v2 폴더 선택
2. Gradle sync 완료 대기
3. google-services.json, strings.xml 교체
4. ▶ Run (에뮬레이터 또는 실제 기기)
```

---

## ✅ 기능 목록

| 탭 | 기능 | 변경사항 |
|----|------|---------|
| 📊 분류 | 축뼈대/팔다리뼈대 보기, 클릭 시 상세 | **+ FAB로 단어 바로 추가 가능** ✨ |
| ⚡ 비교 | 헷갈리는 쌍 나란히 보기 | - |
| 📇 카드 | 플래시카드 뒤집기 애니메이션 | - |
| 🎯 퀴즈 | 4지선다 + 직접 입력 | - |
| 📈 통계 | 오답 누적, 취약 카테고리, 틀린 단어 순위 | Firestore 저장 |
| ✏️ 단어 | 추가/수정/삭제/초기화 | Firestore 저장 |

---

## 💾 Firestore 구조

```
users/
  {uid}/
    bones: [ { id, k, l, axial, cat, desc, tip, story }, ... ]
    stats: { "cranium": 3, "femur": 1, ... }
```

---

## 📦 기술 스택

| | |
|--|--|
| UI | Jetpack Compose + Material 3 |
| 상태관리 | ViewModel + StateFlow |
| 인증 | Firebase Auth (Google Sign-In) |
| DB | Cloud Firestore |
| 최소 SDK | API 26 (Android 8.0+) |
