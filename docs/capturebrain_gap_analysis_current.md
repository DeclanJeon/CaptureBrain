# CaptureBrain Current Gap Analysis

작성 목적: JOB-260527144814926984 기준으로 현재 저장소를 다시 분석해 구현 완료/부분 구현/미구현 항목을 구분하고, 이후 구현팀과 QA가 같은 기준으로 작업할 수 있게 한다.

## 1. 요약 판정

현재 저장소는 PRD 문서만 있는 상태가 아니라 Android/Kotlin scaffold, Compose UI, Room/WorkManager/MediaStore/OCR/Markdown/Google Drive 업로드 marker와 일부 실제 구현이 들어간 상태다.

다만 아직 출시 가능한 MVP는 아니다. 핵심 이유는 다음 네 가지다.

1. 현재 실행 환경에 `java`, `gradle`, `./gradlew`, `adb`, `keytool`이 없어 Android build/test/lint/signingReport/device smoke를 실행하지 못했다.
2. UI에는 current job frontend report `26052714:53:37` 기준 이미지 권한 요청/거부/제한 모드 marker가 추가되었지만, 아직 샘플 상태 기반이며 실제 Repository/Room/Worker 상태와 완전히 연결되지 않았다.
3. current job developer reports `26052714:58:53` 및 `26052715:00:12` 기준으로 `MainActivity.kt`의 duplicate `accountStore`, missing `ContextCompat` import, `screenshotObserver` declaration compile-risk와 `MarkdownGenerator` JSON metadata escaping syntax issue가 보정되었다. 실제 compile 확인은 JDK/Gradle 확보 후 필요하다.
4. screenshot-to-Drive 전체 흐름, Google OAuth 실제 계정 동의, Drive folder/file 생성, offline retry, duplicate 방지는 실제 기기에서 검증되지 않았다.
5. 사용자 요청의 “codex imagen UI 레퍼런스 생성 후 그대로 재현” 산출물은 아직 이 저장소에 없다. 이 작업은 frontend-developer가 UI reference artifact를 만든 뒤 Compose에 반영하고 증거를 남겨야 한다.

## 2. 검증한 파일/마커

검증 기준 파일:

- `README.md`
- `docs/capturebrain_android_prd.md`
- `docs/capturebrain_android_architecture.md`
- `docs/capturebrain_android_work_order.md`
- `docs/capturebrain_android_checklist.md`
- `docs/capturebrain_google_drive_setup.md`
- `docs/capturebrain_google_cloud_oauth_steps.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/ponslink/capturebrain/**`
- `scripts/setup_android_build_env.sh`
- `scripts/verify_capturebrain_android_env.sh`

정적 확인 결과:

- package/applicationId: `com.ponslink.capturebrain`으로 정렬됨
- Kotlin package: `com.ponslink.capturebrain` 하위로 정렬됨
- Android permission: `READ_MEDIA_IMAGES` 존재
- UI: `MaterialTheme`, `Scaffold`, `LazyColumn`, Onboarding/Home/Detail/Settings marker 존재
- DataStore: `CaptureSettingsStore` 존재
- MediaStore/ContentObserver: scanner 및 observer 파일 존재
- WorkManager: `CaptureProcessWorker`, `NetworkType.CONNECTED` marker 존재
- Room: entity/DAO/database marker 존재
- OCR: `OcrProcessor`, `MlKitOcrProcessor` 존재
- Markdown: `MarkdownGenerator` 존재
- Drive: `DriveAccountStore`, `UserGoogleDriveUploader`, `UnconnectedDriveUploader`, `GoogleAccountCredential`, `DriveScopes.DRIVE_FILE`, `files().create` marker 존재
- Forbidden secret marker: `service_account`, `client_secret`, `refresh_token`, `private_key`, `access_token`은 Kotlin source scan에서 없음
- `GoogleDriveUploaderStub()`는 현재 source scan에서 없음

## 3. 구현 상태 분류

### 3.1 완료 또는 정적 구현 확인

| 영역 | 현재 상태 | 증거 |
|---|---|---|
| Android 프로젝트 골격 | 구현됨, 빌드 미검증 | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, Manifest |
| package/applicationId 정렬 | 구현됨 | `app/build.gradle.kts`의 `namespace`/`applicationId` = `com.ponslink.capturebrain` |
| Google Drive 사용자 계정 방식 | 구현됨, 실기기 미검증 | `MainActivity.kt`, `DriveAccountStore.kt`, `UserGoogleDriveUploader.kt` |
| 최소 Drive scope | 구현됨 | `DriveScopes.DRIVE_FILE` marker |
| Drive stub 제거 | 구현됨 | `GoogleDriveUploaderStub()` absent, `UserGoogleDriveUploader` present |
| 기본 Compose 화면 | 구현됨 | Onboarding/Home/Detail/Settings screen marker |
| 이미지 권한/제한 모드 UI | 부분 구현 | frontend report `26052714:53:37`, `RequestPermission`, `ImagePermissionCard`, `LimitedModeCard` marker |
| 설정 저장소 | 구현됨 | `CaptureSettingsStore` DataStore |
| Markdown generator | 구현됨 | `MarkdownGenerator.kt` |
| OCR processor abstraction | 구현됨 | `OcrProcessor.kt`, `MlKitOcrProcessor.kt` |
| 환경 준비 스크립트 | 구현됨 | `scripts/setup_android_build_env.sh`, `scripts/verify_capturebrain_android_env.sh` |

### 3.2 부분 구현

| PRD/영역 | 부분 구현 내용 | 남은 작업 |
|---|---|---|
| FR-001 스크린샷 감지 | MediaStore scanner, ContentObserver 파일 존재 | Activity/Application lifecycle에 실등록되는지 확인, 기기 screenshot smoke |
| FR-002 중복 방지 | `imageHash`/duplicate marker 존재 | DB unique constraint 및 재스캔 테스트 필요 |
| FR-003 자동 감지 ON/OFF | UI switch와 DataStore setting 존재 | UI state와 observer/worker 제어 연결 검증 필요 |
| FR-010~012 권한 설명/거부/재허용 | current job frontend report 기준 `RequestPermission`, `ImagePermissionCard`, `LimitedModeCard`, permission-denied copy marker 존재 | 실제 Android 권한 grant/deny smoke, compile 확인 필요 |
| FR-004 최근 N일 가져오기 | DataStore `recentImportDays` 존재 | manual import use case와 UI action 연결 필요 |
| FR-020 OCR | ML Kit processor marker 존재 | 샘플 이미지 OCR 테스트 필요 |
| FR-030~033 제목/분류/태그/요약 | rule-based fallback/local analyzer marker 존재 | AI client/JSON validator 또는 명확한 OCR-only MVP 기준 확정 필요 |
| FR-034~035 민감정보 | UI copy와 sensitive marker 존재 | 실제 detector 결과로 upload hold, confirm UI, 테스트 필요 |
| FR-040~045 Markdown | generator와 preview marker 존재 | snapshot/unit test, 실제 파일 저장 경로 검증 필요 |
| FR-050~053 Drive | Google Sign-In/Drive uploader 구현 marker 존재 | Google Cloud Android OAuth Client + SHA-1 + 실제 upload smoke 필요 |
| FR-054 재시도 | WorkManager/retry marker 존재 | offline → failed_upload → online retry smoke 필요 |
| FR-055 루트 폴더명 | DataStore setting 존재 | UI 저장/Drive uploader 반영 검증 필요 |
| FR-056 Wi-Fi only | setting 존재, 기본 `CONNECTED` 및 Wi-Fi-only `UNMETERED` marker 존재 | `wifiOnlyUpload=true` runtime/device 네트워크 제약 검증 필요 |
| FR-070~074 처리 이력 | Room/DAO/entity marker 존재 | 실제 state transition tests와 UI list 연결 필요 |

### 3.3 미구현 또는 증거 부족

| 영역 | 미구현/증거 부족 내용 | 담당 권장 |
|---|---|---|
| Gradle wrapper | `./gradlew` 없음 | developer |
| Kotlin compile risk | current job developer reports `26052714:58:53` 및 `26052715:00:12` 기준으로 `MainActivity.kt` duplicate `accountStore`, missing `ContextCompat` import, undeclared `screenshotObserver` 가능성과 `MarkdownGenerator` JSON metadata escaping syntax issue가 보정됨; 실제 compile은 JDK/Gradle 부재로 미검증 | qa |
| 빌드/테스트 검증 | `java`, `gradle`, `keytool`, `adb` 없음 | developer + qa |
| UI imagen reference | codex imagen 결과물/레퍼런스 파일 없음 | frontend-developer |
| 실제 UI 레퍼런스 재현 | 현재 UI는 Material3 scaffold 기반, 이미지 레퍼런스와의 pixel/structure match 증거 없음 | frontend-developer |
| 실제 screenshot-to-Drive E2E | 권한→감지→OCR→Markdown→Drive upload 전체 smoke 없음 | developer + qa |
| Google Cloud OAuth | Android OAuth Client ID/SHA-1/test consent smoke 없음 | developer/owner + qa |
| 테스트 코드 | `*Test*` 파일 scan 결과 없음 | developer + frontend-developer |
| AI 운영 정책 | BYOK/server proxy/no-AI MVP 최종 결정 필요 | ceo + developer |
| Play Store 준비 | privacy policy/data safety/release signing 미완료 | tech-writer + qa |

## 4. 우선순위 작업지시

### P0: 빌드 가능한 개발 환경과 wrapper 확보

담당: developer

작업:

1. `scripts/setup_android_build_env.sh` 실행 또는 로컬 Android Studio/JDK 환경 준비
2. Gradle wrapper 생성 후 저장소에 `gradlew`, `gradle/wrapper/**` 추가
3. 다음 명령 실행 가능하게 만들기

```bash
./gradlew test
./gradlew assembleDebug
./gradlew lintDebug
./gradlew signingReport
```

완료 기준:

- QA가 같은 명령을 재실행 가능
- build/test/lint 결과가 report에 첨부됨

### P1: UI design reference 생성 및 Compose 재현

담당: frontend-developer

작업:

1. codex imagen skill을 사용해 CaptureBrain 모바일 UI reference를 생성한다.
2. 생성 결과를 `docs/ui_reference/` 또는 `docs/capturebrain_ui_reference.md`에 저장한다.
3. reference 기준으로 Onboarding/Home/Detail/Settings를 재정렬한다.
4. 색상, 카드, spacing, typography, navigation, empty/loading/error/sensitive states를 README/checklist와 맞춘다.

완료 기준:

- reference prompt/결과/적용 요약이 문서화됨
- Compose UI 파일에 reference 반영 marker가 있음
- QA가 UI reference artifact 존재와 주요 화면 구조를 확인 가능

### P2: Runtime pipeline 연결

담당: developer

작업:

1. 권한 허용 후 ContentObserver 등록/해제 확인
2. manual recent import action 연결
3. scanner → Room insert → WorkManager enqueue → OCR → Markdown → Drive upload flow 연결
4. duplicate guard 및 status transition 테스트 추가
5. `wifiOnlyUpload=true`일 때 `NetworkType.UNMETERED` runtime 동작 검증

완료 기준:

- 샘플/실기기 screenshot 1개가 `completed` 또는 명확한 실패 상태로 이동
- 같은 screenshot 재처리 시 duplicate 발생하지 않음
- offline 상태에서 upload 실패 후 online retry 가능

### P3: Google OAuth/Drive smoke

담당: developer + qa + owner credential

작업:

1. Google Cloud에서 Android OAuth Client 생성
2. package name `com.ponslink.capturebrain`과 debug SHA-1 등록
3. test user `syas0301@gmail.com`으로 로그인
4. `drive.file` scope만 요청되는지 확인
5. Google Drive에 image, `note.md`, `metadata.json` 생성 확인

완료 기준:

- Drive folder/file IDs가 Room에 저장됨
- CaptureBrain 서버나 service account 없이 사용자 소유 Drive에만 업로드됨
- disconnect 후 upload가 deterministic blocked/failure state로 표시됨

### P4: QA/릴리즈 문서

담당: tech-writer + qa

작업:

1. `docs/capturebrain_android_checklist.md`를 실제 통과/실패 evidence 기준으로 갱신
2. README의 “현재 제한사항/다음 작업”에서 이미 완료된 항목 제거
3. Privacy/Data Safety 초안 작성
4. device smoke 결과를 체크리스트에 반영

완료 기준:

- README와 checklist가 최신 구현 상태와 모순되지 않음
- QA rerun이 문서 stale phrase 때문에 실패하지 않음

## 5. QA rerun 기준

현재 job의 QA 실패 중 tech-writer가 해소해야 할 부분:

- current job tech-writer report 없음 → 본 보고서 및 role report로 해소
- docs/checklist에 current-state evidence 필요 → 이 gap analysis로 보강
- codex imagen UI reference 증거 없음 → tech-writer가 미구현으로 명시하고 frontend-developer handoff로 고정

QA rerun 전에 필요한 최소 산출물:

- developer current-job reports `26052714:58:53` 및 `26052715:00:12` 접수 완료; QA는 최신 developer/frontend/tech-writer reports 기준으로 rerun
- frontend-developer current-job report, 특히 UI reference artifact 여부
- tech-writer current-job report
- 가능하면 `./gradlew`/JDK 확보 후 build/test/lint 결과

빌드 도구가 계속 없으면 QA는 static deliverable 기준으로만 판정하고, build/device/OAuth는 risk로 남겨야 한다.
