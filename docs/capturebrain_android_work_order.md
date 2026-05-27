# CaptureBrain Android Work Order

문서 목적: PRD와 아키텍처를 실제 구현 작업 단위로 쪼개고, 필요한 Android/Google/API 스펙과 단계별 작업 지시를 정의한다.

## 1. 현재 상태

현재 저장소에는 PRD/설계 문서와 Android Gradle/Kotlin scaffold가 존재한다.

- `docs/capturebrain_android_prd.md`
- `docs/capturebrain_android_architecture.md`
- `docs/capturebrain_android_work_order.md`
- `docs/capturebrain_android_checklist.md`
- `README.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/ponslink/capturebrain/**`

developer reports `26052623:43:37` and `26052623:44:16` confirmed the Android/Kotlin foundation: Gradle settings/build files, AndroidManifest, Room database layer, MediaStore scanner plus ContentObserver trigger, WorkManager worker, ML Kit OCR processor, Markdown generator, folder resolver, repository pipeline, and Drive uploader interface/stub. frontend-developer report `26052700:37:29` and developer report `26052700:39:08` then added the user-owned Drive implementation lane: `DriveAccountStore`, Google Sign-In UI/helper markers, `UserGoogleDriveUploader`, `UnconnectedDriveUploader`, `DriveScopes.DRIVE_FILE`, `NetworkType.CONNECTED`, and Gradle `namespace`/`applicationId` alignment to `com.ponslink.capturebrain`. 남은 차이는 `java`, `gradle`, `./gradlew` 부재로 빌드/테스트가 아직 실행되지 않았고, Google Cloud OAuth Client/SHA-1/device Drive smoke가 아직 수행되지 않았다는 점이다. current job frontend report `26052714:53:37`로 이미지 권한 요청/거부/제한 모드 UI marker가 추가되었고, CEO가 `MainActivity.kt`의 duplicate `accountStore`, missing `ContextCompat`, `screenshotObserver` declaration compile-risk를 패치했다. 실제 build/permission runtime은 JDK/Gradle/device 확보 후 확인해야 한다.

## 2. 필요한 외부 설정

### Android 개발 환경

- Android Studio 최신 stable 또는 Gradle CLI
- JDK 17
- Android Gradle Plugin 8.x
- Kotlin 2.x 또는 프로젝트 생성 시 안정 버전
- minSdk: 26 권장
- targetSdk: 최신 stable 권장

### Google Cloud / Drive

필요 작업:

1. Google Cloud Project 생성
2. OAuth consent screen 설정
3. Android OAuth Client ID 생성
   - package name: 현재 확정값은 `com.ponslink.capturebrain`
   - SHA-1 signing certificate fingerprint 필요
4. Drive API v3 활성화
5. OAuth scope: `https://www.googleapis.com/auth/drive.file`
6. 테스트 사용자 `syas0301@gmail.com` 등록, 앱 검수 필요 여부 확인

필요 config:

```txt
Android OAuth Client ID
DRIVE_ROOT_FOLDER_NAME=CaptureBrain
AI_PROVIDER=none|openai|gemini|custom
AI_API_KEY=개발자 로컬/CI secret, 앱에 하드코딩 금지
```

Android 앱에서는 API key/client secret/service account JSON을 직접 포함하지 않는 것이 원칙이다. Drive는 서비스계정이 아니라 사용자 OAuth로 연결한다. AI Vision을 유료 API로 운영하려면 서버 프록시 또는 사용자 BYOK 정책 중 하나를 결정해야 한다. Google Drive/OAuth 설정 상세 절차는 `docs/capturebrain_google_drive_setup.md`를 기준으로 한다.

## 3. 필요한 Android API / 라이브러리

| 목적 | API/라이브러리 | 작업 |
|---|---|---|
| 이미지 권한 | READ_MEDIA_IMAGES, Scoped Storage | 권한 요청/거부 UI |
| 신규 이미지 감지 | MediaStore, ContentObserver | Screenshots 신규 row 감시 |
| 누락 보완 | WorkManager PeriodicWorkRequest | 최근 N일 스캔 |
| 큐/재시도 | WorkManager OneTimeWorkRequest | OCR/업로드 작업 큐 |
| 로컬 상태 | Room | capture_items 테이블 |
| 설정 | DataStore Preferences | ON/OFF, Wi-Fi only, 템플릿 |
| OCR | ML Kit Text Recognition | ko/en/ja 텍스트 추출 |
| 로그인 | Google Identity Services | 계정 선택/OAuth 동의 |
| Drive | Google Drive API v3 REST/Client | 폴더 생성, 파일 업로드/update |
| UI | Jetpack Compose | Onboarding/Home/Detail/Settings |
| 테스트 | JUnit, Turbine, MockWebServer, Compose UI Test | 단위/통합/UI 테스트 |

## 4. 선택형 AI API 계약

AI는 필수가 아니다. 앱은 AI API 없이도 OCR Markdown Drive 저장을 완료해야 한다.

### 요청 JSON

```json
{
  "ocrText": "string",
  "layoutBlocks": [
    {
      "text": "string",
      "boundingBox": {"left": 0, "top": 0, "right": 0, "bottom": 0}
    }
  ],
  "metadata": {
    "fileName": "Screenshot_20260526_143200.png",
    "capturedAt": "2026-05-26T14:32:00+09:00",
    "width": 1080,
    "height": 2400
  },
  "rules": [
    "Preserve original text",
    "Do not invent missing text",
    "Return strict JSON"
  ]
}
```

### 응답 JSON

```json
{
  "title": "AI Agent Architecture",
  "category": "03_Development",
  "subcategory": "Architecture",
  "folderPath": "03_Development/Architecture",
  "summary": "짧은 요약",
  "keyPoints": ["핵심 1", "핵심 2"],
  "tags": ["AI", "Agent", "Architecture"],
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
  },
  "confidence": 0.86
}
```

### 실패 처리

- timeout: 기본 20초 이하
- invalid JSON: retry 1회 후 fallback
- quota/payment error: AI 비활성 경고 표시, OCR Markdown은 계속 진행
- sensitivity true: 설정에 따라 업로드 전 확인 대기

## 5. 구현 단계

### Phase 0: 프로젝트 골격

담당: developer 또는 frontend-developer

작업:

1. Android Gradle 프로젝트 생성
2. package/applicationId 확정
3. Compose, Room, WorkManager, ML Kit, Google auth/Drive 의존성 추가
4. 기본 MainActivity와 Navigation 구성
5. README의 build/test 명령을 실제 Gradle 명령으로 갱신

완료 기준:

- `./gradlew test` 실행 가능
- `./gradlew assembleDebug` 실행 가능
- 빈 앱이 에뮬레이터에서 실행 가능

### Phase 1: 로컬 자동 수집

작업:

1. 이미지 권한 요청 UI
2. MediaStore query 구현
3. ContentObserver 등록/해제
4. Screenshots 경로 필터
5. imageHash 계산
6. Room capture_items 저장
7. 중복 방지
8. 수동 최근 N일 스캔
9. Home 목록/상태 카드

완료 기준:

- 테스트 이미지/에뮬레이터 스크린샷이 queued 상태로 저장됨
- 같은 이미지를 다시 스캔해도 중복 저장되지 않음
- 권한 거부 시 제한 모드 표시

### Phase 2: OCR + Markdown

작업:

1. ML Kit OCR Processor 구현
2. OCR 결과 line/block 모델링
3. Markdown Generator 구현
4. metadata.json 생성
5. 로컬 앱 저장소에 note.md/metadata.json 저장
6. Detail 화면에 OCR/Markdown 미리보기
7. OCR 실패 상태/재시도 구현

완료 기준:

- 샘플 이미지에서 OCR 텍스트 추출
- Markdown에 Original Text와 Summary 영역이 분리됨
- OCR 실패 시 원본과 실패 상태가 보존됨

### Phase 3: Google Drive 업로드

작업:

1. package/applicationId를 Kotlin package 및 Google Cloud Android OAuth Client package name과 정렬
2. Google 로그인/OAuth 동의
3. `DriveScopes.DRIVE_FILE`만 사용하는 사용자 Drive credential 생성
4. Drive root folder 생성/검색
5. 카테고리/날짜/capture_slug 폴더 생성
6. 원본 이미지 업로드
7. note.md 업로드
8. metadata.json 업로드
9. WorkManager `NetworkType.CONNECTED` 기본 제약과 Wi-Fi only 시 `NetworkType.UNMETERED` 반영
10. 업로드 실패 재시도/backoff

완료 기준:

- 실제 Google Drive에 CaptureBrain 폴더와 파일 3개가 생성됨
- Drive file id가 Room에 저장됨
- 네트워크 실패 시 failed_upload 후 재시도 가능
- 서비스계정/embedded secret 없이 테스트 사용자 `syas0301@gmail.com`의 Drive에만 저장됨

### Phase 4: AI 분류/요약

작업:

1. AI 분석 설정 ON/OFF
2. 규칙 기반 fallback FolderResolver
3. 선택형 AI API client 인터페이스
4. strict JSON parser/validator
5. 민감정보 규칙 기반 detector
6. sensitive_pending 상태와 확인 UI
7. 카테고리 수정 UI와 향후 추천 반영용 저장

완료 기준:

- AI OFF 상태에서도 Inbox/규칙 기반 저장 가능
- AI ON 상태에서 title/category/tags/summary 반영
- 민감정보 후보는 업로드 전 멈춤 옵션 제공

### Phase 5: 안정화/릴리즈 준비

작업:

1. 테스트 커버리지 보강
2. Crashlytics 또는 로컬 에러 리포트 선택
3. 개인정보 처리방침 초안
4. Play Store 권한/데이터 안전 섹션 준비
5. 배터리/백그라운드 제조사별 테스트
6. 접근성/다국어 문자열 정리

완료 기준:

- 주요 단위/통합/UI 테스트 통과
- 실제 Android 13+ 기기에서 스크린샷 감지 → Drive 저장 성공
- 알려진 제한사항이 README에 명시됨

## 6. 역할별 작업 지시

### developer

우선순위:

1. Gradle/Android 프로젝트 생성
2. Room/DataStore/WorkManager 기반 데이터 파이프라인
3. MediaStore/ContentObserver/수동 스캔
4. Drive API 데이터 계층

수정 예상 파일:

- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/.../data/**`
- `app/src/main/java/.../worker/**`
- `app/src/test/**`

### frontend-developer

우선순위:

1. Compose Navigation
2. Onboarding/Home/Detail/Settings UI
3. 권한 요청/거부 UX: current job에서 `RequestPermission`, `ImagePermissionCard`, `LimitedModeCard` marker 추가됨; build/device 검증 필요
4. codex imagen UI reference 생성 및 reference 기반 재현
5. Markdown preview UI
6. 상태/실패/재시도 UX

수정 예상 파일:

- `app/src/main/java/.../ui/**`
- `app/src/main/res/values/strings.xml`
- `app/src/androidTest/**`

### tech-writer

우선순위:

1. 아키텍처/작업지시서/체크리스트 유지
2. README를 실제 구현 상태에 맞춰 갱신
3. credential/device dependent 제한사항 명시
4. QA가 실행할 명령과 증거 항목 정리

### qa

우선순위:

1. Gradle build/test 확인
2. PRD 요구사항 체크리스트 추적
3. 샘플 이미지 OCR/Markdown 검증
4. Drive 업로드 수동/계정 테스트
5. 권한 거부/네트워크 실패/중복 방지/재시도 테스트

## 7. QA 명령 초안

프로젝트 scaffold는 존재한다. 다만 현재 실행 환경에는 `java`, `gradle`, `./gradlew`가 없어 아래 명령은 아직 실행되지 않았다. Gradle wrapper/JDK가 준비되면 README와 이 문서의 명령을 실제 통과 명령으로 갱신해야 한다.

예상 명령:

```bash
./gradlew test
./gradlew connectedDebugAndroidTest
./gradlew assembleDebug
```

선택 검증:

```bash
./gradlew lintDebug
```

현재는 Gradle 프로젝트 파일은 있으나 JDK/Gradle wrapper가 없으므로 위 명령은 이 환경에서 실행 불가하다.

## 8. 구현 리스크와 대응

| 리스크 | 대응 |
|---|---|
| 제조사별 백그라운드 제한으로 감지 누락 | ContentObserver + PeriodicWorkManager 스캔 병행 |
| Android 권한 정책 변화 | targetSdk 기준 권한 분기, 전체 파일 접근 금지 |
| Drive OAuth 검수/설정 지연 | drive.file 최소 범위, 개발용 테스트 사용자 먼저 등록 |
| OCR 정확도 낮음 | 원본 이미지 보존, 재처리, 수동 수정 여지 |
| AI API 비용/개인정보 | AI 기본 선택형, OCR-only 완성 경로 보장 |
| 중복 업로드 | mediaStoreId + imageHash + Drive file id 저장 |
| 민감정보 업로드 | 규칙 기반 감지 + 사용자 확인 옵션 |

## 9. 구현 전 결정 필요 사항

1. applicationId/package 확정 완료: Kotlin package와 Gradle `namespace`/`applicationId`는 `com.ponslink.capturebrain`으로 통일. 사용자 제공 후보 `com.ponslink.com.capturebrain`은 채택하지 않음
2. minSdk: 26으로 시작할지 여부
3. AI API 운영 방식: 앱 내 BYOK, 서버 프록시, 또는 MVP 제외
4. Google OAuth client 생성 주체와 SHA-1 fingerprint 제공 방식
5. 실제 테스트용 Google 계정/Drive 폴더 사용 정책
6. Play Store 출시를 당장 목표로 할지, 내부 APK 검증을 먼저 할지
