# CaptureBrain Android Architecture

문서 목적: `capturebrain_android_prd.md`를 구현 가능한 Android MVP 설계로 변환한다.

## 1. 구현 목표

MVP 핵심 경로는 다음 하나다.

```txt
스크린샷 감지 → 로컬 큐 저장 → OCR → Markdown 생성 → Google Drive 업로드 → 처리 이력 표시
```

현재 저장소에는 PRD/설계 문서와 Android Gradle/Kotlin scaffold, 사용자 소유 Google Drive OAuth 업로드 구현이 존재한다. 다만 `./gradlew`, `java`, `gradle` 부재로 빌드 검증은 아직 불가하고, 실제 기기 OAuth/Drive 업로드 smoke가 남아 있다. 이 문서는 구조와 검증 기준을 정의한다.

## 2. 권장 기술 스택

| 영역 | 선택 | 이유 |
|---|---|---|
| 언어 | Kotlin | Android 표준, Compose/Coroutines/WorkManager 친화 |
| UI | Jetpack Compose + Material 3 | 빠른 MVP UI, 상태 기반 렌더링 |
| 앱 구조 | MVVM + Repository | UI, 도메인, 데이터 계층 분리 |
| 비동기 | Kotlin Coroutines + Flow | 큐/상태 변경 스트림 처리 |
| 백그라운드 | WorkManager | 지연/재시도/네트워크 조건 처리 |
| 로컬 DB | Room | 처리 이력, 중복 방지, 큐 상태 저장 |
| 이미지 감지 | MediaStore + ContentObserver + 보완 스캔 | 신규 감지와 누락 복구 병행 |
| OCR | Google ML Kit Text Recognition v2 | 온디바이스 기본 OCR, 개인정보/비용 최소화 |
| Drive | Google Identity Services + Google Drive API v3 | 사용자의 Drive 폴더에 이미지/Markdown 저장 |
| 민감정보 감지 | MVP: 규칙 기반 + 선택 AI | 이메일/전화/카드/주민번호 패턴 우선 |
| AI 요약/분류 | 선택형 Vision/Text LLM API | 기본 기능은 AI 없이도 동작해야 함 |

## 3. 모듈 구조

권장 패키지 구조:

```txt
app/src/main/java/com/ponslink/capturebrain/
  CaptureBrainApp.kt
  MainActivity.kt
  core/
    config/AppConfig.kt
    dispatchers/DispatcherProvider.kt
    errors/AppError.kt
    time/Clock.kt
  data/
    local/CaptureBrainDatabase.kt
    local/CaptureItemEntity.kt
    local/CaptureDao.kt
    media/ScreenshotMediaStoreDataSource.kt
    drive/GoogleDriveDataSource.kt
    ocr/MlKitOcrDataSource.kt
    ai/AiAnalysisDataSource.kt
    settings/SettingsDataStore.kt
  domain/
    model/CaptureItem.kt
    model/CaptureStatus.kt
    model/OcrResult.kt
    model/AnalysisResult.kt
    repository/CaptureRepository.kt
    usecase/DetectNewScreenshotUseCase.kt
    usecase/ImportRecentScreenshotsUseCase.kt
    usecase/ProcessCaptureUseCase.kt
    usecase/RetryFailedCaptureUseCase.kt
    usecase/GenerateMarkdownUseCase.kt
    usecase/ResolveFolderUseCase.kt
  worker/
    ScreenshotScanWorker.kt
    CaptureProcessWorker.kt
    DriveUploadWorker.kt
  observer/
    ScreenshotContentObserver.kt
  ui/
    onboarding/
    home/
    detail/
    settings/
```

## 4. 데이터 흐름

```txt
1. ContentObserver 또는 수동 스캔이 MediaStore 이미지 row를 발견한다.
2. ScreenshotMediaStoreDataSource가 Screenshots 경로/파일명/날짜 조건을 필터링한다.
3. Repository가 mediaStoreId + imageHash로 중복을 차단하고 CaptureItem을 queued 상태로 Room에 저장한다.
4. WorkManager가 CaptureProcessWorker를 실행한다.
5. ImageLoader가 content:// URI를 안전하게 열고 OCR 입력 이미지를 만든다.
6. MlKitOcrDataSource가 OCR 텍스트/블록/라인/신뢰도 대체값을 생성한다.
7. 선택 옵션이 켜져 있으면 AiAnalysisDataSource가 제목/카테고리/태그/요약/민감도 JSON을 생성한다.
8. AI가 꺼져 있거나 실패하면 규칙 기반 FolderResolver가 Inbox 또는 기본 카테고리로 fallback한다.
9. GenerateMarkdownUseCase가 원문 보존 Markdown과 metadata.json 내용을 만든다.
10. DriveUploadWorker가 루트/카테고리/날짜 폴더를 만들고 원본 이미지, note.md, metadata.json을 업로드한다.
11. Room 상태를 completed 또는 failed_*로 업데이트한다.
12. UI는 Flow를 구독해 홈/상세/설정 화면에 최신 상태를 표시한다.
```

## 5. Android 권한 및 OS 정책

### 필수 권한

| Android 버전 | 권한/방식 | 구현 메모 |
|---|---|---|
| Android 13+ | `READ_MEDIA_IMAGES` | 이미지 읽기 권한. 권한 거부 시 제한 모드 표시 |
| Android 10~12 | Scoped Storage + MediaStore | 전체 파일 접근 권한 금지 |
| Android 9 이하 | `READ_EXTERNAL_STORAGE` 고려 | MVP 최소 지원 SDK 결정 후 범위 조정 |
| 모든 버전 | 인터넷 | Drive/API 업로드용 |
| Android 13+ | `POST_NOTIFICATIONS` 선택 | 처리 완료/실패 알림을 제공할 경우만 요청 |

### 백그라운드 실행 원칙

- 실시간 장기 서비스보다 ContentObserver + WorkManager를 우선한다.
- 제조사별 백그라운드 제한으로 감지가 누락될 수 있으므로 주기적 보완 스캔을 둔다.
- 업로드는 네트워크 조건과 Wi-Fi only 설정을 WorkManager Constraints로 반영한다.
- 배터리 최적화 제외 요청은 MVP 기본 요구사항이 아니다. 필요 시 사용자에게 선택적으로 안내한다.

## 6. Room 로컬 DB 스키마

### capture_items

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | TEXT PK | UUID |
| media_store_id | TEXT | MediaStore row id |
| image_uri | TEXT | content:// URI |
| image_hash | TEXT | 중복 방지 SHA-256 |
| file_name | TEXT | 원본 파일명 |
| captured_at | INTEGER | epoch millis |
| detected_at | INTEGER | epoch millis |
| processed_at | INTEGER nullable | 처리 완료 시각 |
| status | TEXT | detected/queued/processing/markdown_created/uploading/completed/failed_ocr/failed_upload/skipped/sensitive_pending |
| source_app | TEXT nullable | Android에서 신뢰성 낮으므로 unknown 허용 |
| ocr_text | TEXT nullable | OCR 원문 |
| ocr_confidence | REAL nullable | 엔진별 신뢰도 대체값 |
| title | TEXT nullable | 생성 제목 |
| summary | TEXT nullable | 요약 |
| category | TEXT nullable | 대분류 |
| subcategory | TEXT nullable | 소분류 |
| tags_json | TEXT | JSON array |
| entities_json | TEXT | JSON object |
| sensitivity_json | TEXT | JSON object |
| markdown_local_path | TEXT nullable | 로컬 note.md 경로 |
| metadata_local_path | TEXT nullable | 로컬 metadata.json 경로 |
| drive_image_file_id | TEXT nullable | Drive 이미지 파일 id |
| drive_markdown_file_id | TEXT nullable | Drive Markdown 파일 id |
| drive_metadata_file_id | TEXT nullable | Drive metadata 파일 id |
| drive_folder_id | TEXT nullable | Drive 폴더 id |
| error_message | TEXT nullable | 사용자 표시 가능한 실패 이유 |
| retry_count | INTEGER | 재시도 횟수 |
| created_at | INTEGER | 생성 시각 |
| updated_at | INTEGER | 수정 시각 |

필수 인덱스:

- unique(media_store_id)
- unique(image_hash)
- index(status)
- index(captured_at)
- index(category, subcategory)

### app_settings

DataStore Preferences 권장:

- automation_enabled: Boolean
- drive_root_folder_name: String, 기본 `CaptureBrain`
- wifi_only_upload: Boolean
- ai_analysis_enabled: Boolean
- sensitive_confirmation_enabled: Boolean
- ocr_languages: Set<String>, 기본 ko/en/ja
- import_recent_days: Int
- excluded_paths: Set<String>
- excluded_apps: Set<String>, source_app 신뢰성 한계로 보조 설정
- markdown_template: String

## 7. OCR 설계

- 기본 엔진은 ML Kit 온디바이스 Text Recognition.
- OCR 결과는 라인 단위 순서를 보존한다.
- Markdown에는 `Original Text`와 `Layout-Preserved Notes`를 분리한다.
- OCR 실패 시 원본 이미지는 보존하고 상태를 `failed_ocr`로 저장한다.
- 재처리 버튼은 같은 CaptureItem에 대해 OCR/AI/Markdown/Upload 작업을 다시 큐에 넣는다.

## 8. AI 분석 설계

AI는 MVP의 필수 성공 경로가 아니라 보조 기능이다.

### 입력

- OCR 원문
- OCR 블록/라인 위치 정보
- 이미지 메타데이터: 파일명, 크기, 캡처 시각
- 선택 시 축소 이미지 또는 Vision API input

### 출력 JSON

```json
{
  "title": "string",
  "category": "01_Learning|02_Work|03_Development|04_Business|05_Content|06_Shopping|07_Personal|00_Inbox|99_Archive",
  "subcategory": "string",
  "folderPath": "string",
  "summary": "string",
  "keyPoints": ["string"],
  "tags": ["string"],
  "entities": {
    "people": [],
    "products": [],
    "urls": [],
    "dates": [],
    "prices": []
  },
  "sensitivity": {
    "isSensitive": false,
    "reason": null
  }
}
```

### 실패 정책

- AI 실패는 전체 처리 실패가 아니다.
- AI 실패 시 제목은 파일명/첫 OCR 라인 기반으로 만들고 폴더는 `00_Inbox`로 저장한다.
- 외부 API를 사용할 경우 설정 화면에서 명확히 고지하고 끌 수 있어야 한다.

## 9. Google Drive 연동 설계

Drive 연동은 개발자 서버/서비스계정이 아니라 사용자 본인의 Google 계정 OAuth로 수행한다. 현재 소스 package와 Gradle `namespace`/`applicationId`는 `com.ponslink.capturebrain`으로 정렬되었다. 자세한 설정 절차는 `docs/capturebrain_google_drive_setup.md`를 기준으로 한다.

### OAuth 범위

MVP 권장 범위:

- `https://www.googleapis.com/auth/drive.file`

이 범위는 앱이 생성/연 파일에 대한 접근만 허용하므로 전체 Drive 접근보다 개인정보 부담이 낮다.

금지:

- 서비스계정 JSON을 앱에 포함하지 않는다.
- client secret, refresh token, API key를 앱/저장소에 하드코딩하지 않는다.
- 전체 Drive scope로 확대하지 않는다.

구현 메모:

- Google Sign-In/Identity Services로 계정을 선택하고 사용자 동의를 받는다.
- Drive client는 `DriveScopes.DRIVE_FILE`만 사용한다.
- 토큰 문자열을 Room/DataStore에 평문 저장하지 않는다.
- Drive upload WorkManager는 기본 `NetworkType.CONNECTED`, Wi-Fi only 설정 시 `NetworkType.UNMETERED` 제약을 사용한다.

### 폴더 규칙

```txt
CaptureBrain/
  00_Inbox/
  01_Learning/{topic}/YYYY/MM/{capture_slug}/
  02_Work/{topic}/YYYY/MM/{capture_slug}/
  03_Development/{Errors|Code|Docs|Architecture}/YYYY/MM/{capture_slug}/
  04_Business/{Competitors|Pricing|Marketing|Ideas}/YYYY/MM/{capture_slug}/
  05_Content/{Writing|Design|Social}/YYYY/MM/{capture_slug}/
  06_Shopping/{Products|Prices}/YYYY/MM/{capture_slug}/
  07_Personal/YYYY/MM/{capture_slug}/
  99_Archive/YYYY/MM/{capture_slug}/
```

업로드 파일:

- 원본 이미지: 원본 확장자 유지
- `note.md`
- `metadata.json`

### 재시도/중복 방지

- Drive folder/file id를 Room에 저장한다.
- 업로드 도중 실패하면 `failed_upload`와 retry_count를 기록한다.
- WorkManager 지수 백오프를 사용한다.
- 같은 CaptureItem의 재업로드는 기존 Drive file id가 있으면 update, 없으면 create 한다.

## 10. Markdown 생성 설계

생성기는 외부 템플릿 엔진 없이 Kotlin 문자열 빌더로 시작한다.

필수 섹션:

```md
# {title}

- Captured at: {captured_at}
- Processed at: {processed_at}
- Source app: {source_app}
- Category: {category}
- Subcategory: {subcategory}
- Language: {language}
- OCR confidence: {ocr_confidence}
- Original image: {image_filename}

---

## Original Text

{ocr_text_preserved}

---

## Layout-Preserved Notes

{layout_preserved_text}

---

## Summary

{summary}

---

## Key Points

{key_points}

---

## Entities

{entities}

---

## Tags

{tags}

---

## Processing Metadata

```json
{metadata_json}
```
```

## 11. UI 화면

### Onboarding

- 제품 설명
- 이미지 권한 요청
- Google 로그인/Drive 권한
- 개인정보/AI 사용 안내
- 자동 감지 시작

### Home

- 자동 감지 ON/OFF
- 오늘 처리 수
- queued/processing/failed/completed 상태 카드
- 최근 10개 캡처 목록
- 수동 스캔, 실패 재시도, Drive 열기

### Detail

- 원본 이미지 미리보기
- 제목/카테고리/태그
- OCR 원문
- Markdown 미리보기
- Drive 저장 위치
- 재처리/제외 버튼

### Settings

- 자동 감지
- 최근 N일 가져오기
- 루트 폴더명
- Wi-Fi only
- OCR 언어
- AI 분석 사용
- 민감정보 확인
- Markdown 템플릿
- 캐시/이력 삭제

## 12. 보안/개인정보 결정

- 전체 파일 접근 권한은 기본 요구하지 않는다.
- OAuth 토큰은 Android Credential Manager/AccountManager 또는 Google Identity Services 권장 저장소를 사용하고, 직접 평문 저장하지 않는다.
- Room에는 최소 메타데이터만 저장한다. OCR 원문은 기능상 필요하지만 삭제 기능을 제공한다.
- 민감정보 후보는 업로드 전 `sensitive_pending`으로 멈출 수 있다.
- 외부 AI API는 기본 OFF 또는 명확한 동의 후 ON으로 둔다.
- 로그에는 OCR 원문, 토큰, Drive file id 전체를 남기지 않는다.

## 13. 구현 상태 구분

현재 이 저장소 기준:

| 항목 | 상태 |
|---|---|
| PRD | 작성됨 |
| 설계 문서 | 이 문서로 작성됨 |
| Android Gradle 프로젝트 | scaffold 파일 존재: settings/root/app Gradle + AndroidManifest. 단, `./gradlew`, `java`, `gradle` 부재로 빌드 미검증 |
| Kotlin 소스 | UI/core/data/worker/ocr/drive marker 파일 존재. developer report 및 컴파일 검증 대기 |
| ML Kit OCR | processor marker 존재, 샘플 OCR/기기 검증 대기 |
| Google Drive 연동 | `UserGoogleDriveUploader` + Google Sign-In/account storage 구현. OAuth 클라이언트와 실제 기기 smoke 필요 |
| AI API 연동 | 미구현, 공급자/키 필요 |
| 디바이스/에뮬레이터 테스트 | 미실행, JDK/Gradle wrapper와 실제 기기/에뮬레이터 필요 |
