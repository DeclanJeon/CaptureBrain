# CaptureBrain Android Implementation Checklist

목적: PRD 요구사항을 구현 작업과 QA 증거로 연결한다.

상태 표기:

- Not started: 아직 구현 없음
- In progress: 코드/문서 일부 존재
- Blocked: 외부 credential/device 또는 선행 구현 필요
- Done: 구현 및 검증 완료

현재 저장소 기준으로 Android scaffold와 일부 Kotlin marker 구현이 존재하고, current job frontend report `26052714:53:37` 기준 이미지 권한 요청/제한 모드 UI marker도 추가되었다. current job developer reports `26052714:58:53` 및 `26052715:00:12` 기준으로 `MainActivity.kt` permission-gated automation/ContentObserver, duplicate `accountStore`/`ContextCompat`/`screenshotObserver` compile-risk 보정, `CaptureProcessWorker` retry input path, `MarkdownGenerator` JSON metadata escaping syntax 보정, DataStore settings, manual import/retry entrypoints, Wi-Fi-only `NetworkType.UNMETERED`, sensitive upload hold가 확인되었다. 현재 Android Studio snap JBR, Android SDK command-line tools/platform-tools, Gradle wrapper를 연결해 `testDebugUnitTest`, `assembleDebug`, `lintDebug`, `signingReport`를 통과했다. Google Drive/OAuth setup 문서(`docs/capturebrain_google_drive_setup.md`)가 추가되었고, frontend-developer report `26052700:37:29` 기준으로 Google Sign-In UI/account flow도 일부 구현되었다. 따라서 상태는 “구현 파일 존재”, “설정 문서 존재”, “검증 완료”를 분리해 추적한다.

## 1. MVP 요구사항 매핑

| PRD ID | 요구사항 | 구현 작업 | QA 증거 | 현재 상태 |
|---|---|---|---|---|
| FR-001 | 새 스크린샷 파일 생성 감지 | MediaStore query + ContentObserver 등록 | 에뮬레이터/기기 스크린샷 후 queued row 생성 | In progress: MediaStore/scanner marker 존재, ContentObserver 실등록/e2e 미검증 |
| FR-002 | 이미 처리한 스크린샷 중복 방지 | mediaStoreId unique + imageHash unique | 같은 파일 재스캔 시 row 1개 유지 | Not started |
| FR-003 | 자동 감지 ON/OFF | DataStore 설정 + observer/worker 제어 | OFF 시 신규 처리 없음, ON 시 처리 재개 | In progress: `CaptureSettingsStore` DataStore와 MainActivity observer/periodic worker 제어 marker 추가, UI 토글/e2e 미검증 |
| FR-004 | 최근 N일 기존 스크린샷 수동 가져오기 | ImportRecentScreenshotsUseCase | 최근 1/7/30일 옵션별 scan 결과 | In progress: worker `importRecentNow(daysBack)`와 DataStore recentImportDays marker 추가, UI/기기 검증 미실행 |
| FR-005 | 감지 실패 보완 스캔 | PeriodicWorkRequest | ContentObserver 누락 fixture를 주기 스캔으로 발견 | In progress: `schedulePeriodicScan`/`cancelPeriodicScan` PeriodicWorkRequest marker 추가, 기기 검증 미실행 |
| FR-010 | 이미지 접근 권한 설명 | Onboarding 권한 안내 UI | 권한 화면 문구 스크린샷/Compose test | In progress: `RequestPermission`, `ImagePermissionCard`, 권한 안내/요청 marker 존재; 실제 권한 화면/device smoke 미검증 |
| FR-011 | 권한 거부 시 제한 모드 | Permission denied state | 거부 후 홈에 제한 모드/재요청 CTA 표시 | In progress: `LimitedModeCard`, permission denied copy, 홈/설정 재요청 CTA marker 존재; 실제 권한 deny smoke 미검증 |
| FR-012 | 나중에 권한 재허용 | Settings permission CTA | 설정에서 시스템 권한 화면 이동 | In progress: Settings의 `ImagePermissionCard` 재요청 CTA marker 존재; Android settings deep-link/device smoke 미검증 |
| FR-013 | 전체 파일 접근 기본 요구 금지 | MANAGE_EXTERNAL_STORAGE 미사용 | AndroidManifest scan | In progress: `READ_MEDIA_IMAGES`/legacy `READ_EXTERNAL_STORAGE` marker만 확인, `MANAGE_EXTERNAL_STORAGE` 미사용 scan은 QA rerun 필요 |
| FR-020 | 이미지 텍스트 추출 | ML Kit OCR Processor | 샘플 이미지 OCR text assertion | In progress: ML Kit OCR 파일 marker 존재, 빌드/샘플 OCR 미검증 |
| FR-021 | OCR 원문 순서 보존 | block/line sorting + markdown original section | 원문 line order snapshot test | Not started |
| FR-022 | 한국어/영어/일본어 OCR | ML Kit 언어 지원 확인/설정 | ko/en/ja 샘플 OCR smoke | Not started |
| FR-023 | OCR 실패 상태와 원본 보존 | failed_ocr status + image uri 유지 | 깨진 이미지 fixture 실패 상태 확인 | Not started |
| FR-024 | OCR 재시도 | RetryFailedCaptureUseCase | failed_ocr → queued/processing 전환 | In progress: current job developer report `26052715:00:12` 기준 `CaptureProcessWorker` retry input path 및 `repository.retryFailed()` marker 존재; runtime transition test 미검증 |
| FR-030 | 제목 생성 | AI 또는 fallback title generator | OCR 첫 줄/AI title 반영 test | Not started |
| FR-031 | 카테고리 자동 분류 | FolderResolver + optional AI | category mapping unit test | Not started |
| FR-032 | 태그 자동 생성 | Keyword/AI tags | tags_json 생성 확인 | Not started |
| FR-033 | 짧은 요약 생성 | AI summary 또는 OCR-only placeholder | AI ON/OFF 별 summary section 확인 | Not started |
| FR-034 | 민감정보 가능성 감지 | regex detector + optional AI | 이메일/전화/카드 fixture detection | Not started |
| FR-035 | 민감정보 업로드 전 확인 | sensitive_pending status + UI confirm | sensitive fixture는 Drive upload 전 멈춤 | Not started |
| FR-040 | 스크린샷마다 Markdown 생성 | GenerateMarkdownUseCase | note.md 파일 생성 | In progress: MarkdownGenerator marker 존재, runtime 생성 미검증 |
| FR-041 | OCR 원문과 AI 요약 분리 | Markdown template sections | Original Text/Summary section snapshot | Not started |
| FR-042 | 원본 이미지 파일명 포함 | Markdown metadata | `Original image:` line assertion | Not started |
| FR-043 | 태그/카테고리 포함 | Markdown metadata/tags | Category/Tags section assertion | Not started |
| FR-044 | Markdown 템플릿 수정 | Settings template editor + DataStore | 사용자 템플릿 반영 test | Not started |
| FR-045 | 사람이 읽을 수 있는 파일명 | slug generator | title/date slug snapshot | Not started |
| FR-050 | Google 계정 로그인 | Google Identity Services | 실제/Mock auth flow | Implemented statically: Google Sign-In/Drive account marker 존재; Google Cloud OAuth Client/SHA-1/device consent smoke 필요 |
| FR-051 | Drive 루트 폴더 생성 | Drive folders.create/search | CaptureBrain root folder id 저장 | Not started |
| FR-052 | 카테고리 하위 폴더 생성 | FolderResolver + Drive API | folder tree 생성 확인 | Not started |
| FR-053 | 원본 이미지와 Markdown 업로드 | multipart upload/update | Drive에 이미지/note.md/metadata.json 존재 | Implemented statically: UserGoogleDriveUploader uploads image/Markdown/metadata with DRIVE_FILE; device OAuth smoke pending |
| FR-054 | 업로드 실패 로컬 큐/재시도 | failed_upload + WorkManager backoff | 네트워크 실패 MockWebServer test | In progress: WorkManager/retry marker 존재, 테스트 미검증 |
| FR-055 | 루트 폴더명 변경 | Settings DataStore | 변경명으로 Drive folder 생성 | In progress: `CaptureSettingsStore.rootFolderName`이 `UserGoogleDriveUploader` rootFolderName으로 전달됨, Settings UI/e2e 미검증 |
| FR-056 | Wi-Fi only 업로드 | WorkManager Constraints | metered network 조건에서 upload 보류 | In progress: `wifiOnlyUpload` 설정 시 WorkManager `NetworkType.UNMETERED`, UI/실기기 네트워크 검증 미실행 |
| FR-060 | 분석 결과로 저장 폴더 결정 | ResolveFolderUseCase | category별 folder path unit test | Not started |
| FR-061 | 없는 폴더 자동 생성 | Drive folder ensure function | 중첩 폴더 생성 smoke | Not started |
| FR-062 | 낮은 신뢰도 Inbox 저장 | confidence threshold | low confidence → 00_Inbox test | Not started |
| FR-063 | 자동 분류 결과 수정 | Detail category editor | 수정 후 DB/Markdown 재생성 | Not started |
| FR-064 | 수정 분류 향후 추천 반영 | correction history 저장 | 동일 패턴 추천 반영 unit test | Not started |
| FR-070 | 처리 상태 로컬 DB 저장 | Room capture_items | status transition unit test | In progress: Room entity/DAO/database marker 존재, unit test 미실행 |
| FR-071 | 최근 처리 이력 표시 | Home list Flow | 최근 10개 표시 Compose test | Not started |
| FR-072 | 실패 항목 재시도 | Retry action | failed_upload/failed_ocr 재시도 | In progress: retry entrypoint/worker marker 존재; failed_upload/failed_ocr device or unit test 미검증 |
| FR-073 | 특정 이미지 제외 처리 | skipped status | 제외 후 worker 미처리 | Not started |
| FR-074 | 중복 파일 재처리 방지 | unique keys + repository guard | hash/mediaStore duplicate test | Not started |

## 2. 비기능 요구사항 체크리스트

| NFR ID | 요구사항 | 구현 작업 | QA 증거 | 현재 상태 |
|---|---|---|---|---|
| NFR-001 | 감지 후 10초 이내 큐 등록 | Observer → DB insert 최적화 | 실제 기기 timestamp delta 측정 | Not started |
| NFR-002 | 일반 스크린샷 30초 내 OCR 목표 | Worker/OCR 성능 측정 | 샘플 10장 평균/최대 시간 | Not started |
| NFR-003 | 배터리 과사용 방지 | WorkManager constraints, 장기 foreground service 회피 | Battery Historian 또는 수동 관찰 | Not started |
| NFR-004 | 대량 처리 안정성 | WorkManager queue/backoff | 100장 batch import smoke | Not started |
| NFR-010 | 네트워크 끊김 로컬 큐 보관 | failed_upload + retry | airplane mode upload test | Not started |
| NFR-011 | 앱 종료 후 대기 항목 유지 | Room persisted queue | 앱 재시작 후 queued 유지 | Not started |
| NFR-012 | 동일 파일 중복 업로드 방지 | Drive file id 저장/update | 재업로드 시 duplicate 파일 없음 | Not started |
| NFR-013 | 지수 백오프 재시도 | WorkManager backoff | work request config assertion | Not started |
| NFR-020 | OAuth 토큰 안전 저장 | Google auth library 사용, 평문 저장 금지 | repo scan: token persistence 없음 | Implemented statically: `DriveAccountStore`는 selected account/root folder만 저장하고 token/secret source marker 없음; Android auth runtime 검증 필요 |
| NFR-021 | 민감 이미지 확인 옵션 | sensitive_pending flow | 민감 fixture upload hold | Not started |
| NFR-022 | 특정 앱/폴더 제외 | excluded paths/apps settings | 제외 path 미처리 test | Not started |
| NFR-023 | 외부 API 사용 고지 | Settings/Onboarding disclosure | UI 문구 확인 | Not started |
| NFR-024 | 최소 메타데이터 저장 | DB schema review | schema에 불필요 원본/토큰 없음 | Not started |

## 3. 구현 산출물 체크리스트

### 프로젝트 골격

- [x] `settings.gradle.kts` exists
- [x] root `build.gradle.kts` exists
- [x] `app/build.gradle.kts` exists
- [x] `app/src/main/AndroidManifest.xml` exists
- [x] `MainActivity.kt` exists
- [x] Compose theme/navigation marker exists
- [x] Gradle wrapper 또는 실행 가능한 Gradle 환경

### 데이터/도메인

- [x] Capture item/data model marker exists
- [x] processing status enum marker exists
- [x] Room entity/DAO/database marker exists
- [x] DataStore settings marker exists: `CaptureSettingsStore`
- [x] Repository implementation marker exists
- [ ] status transition tests

### 감지/큐

- [x] MediaStore query marker exists
- [x] Screenshots path heuristic marker exists
- [x] ContentObserver registration marker exists in `MainActivity.startAutomationFromSettings`
- [x] Process worker marker exists
- [ ] Manual import use case
- [x] hash/duplicate guard marker exists

### OCR/Markdown

- [x] ML Kit dependency/processor marker exists
- [x] OCR processor marker exists
- [ ] OCR result model
- [x] Markdown generator marker exists
- [x] metadata generator marker exists
- [ ] markdown snapshot tests

### Drive

- [x] Google sign-in/auth flow marker exists: `MainActivity.kt`, `GoogleDriveSignInConfig.kt`
- [x] Drive client abstraction marker exists
- [x] Google Drive/OAuth setup doc exists: `docs/capturebrain_google_drive_setup.md`
- [x] package/applicationId aligned with Kotlin package: `com.ponslink.capturebrain`
- [x] `DriveScopes.DRIVE_FILE` implementation marker
- [x] secure per-user account marker exists: `DriveAccountStore` stores selected account/root folder only; OAuth tokens stay with Google services
- [x] root folder ensure marker exists in `UserGoogleDriveUploader`
- [x] category folder ensure marker exists in `UserGoogleDriveUploader`
- [x] file upload/create marker exists: `UserGoogleDriveUploader` uploads image, `note.md`, `metadata.json`
- [x] retry/backoff marker exists
- [x] Wi-Fi only constraints marker: `CaptureSettingsStore.wifiOnlyUpload` switches WorkManager to `NetworkType.UNMETERED`
- [x] WorkManager network constraint marker: `NetworkType.CONNECTED` default and Wi-Fi-only `UNMETERED` path

### AI/분류/민감정보

- [ ] AI client interface
- [ ] strict JSON schema validator
- [x] rule-based fallback classifier marker exists
- [x] sensitive detector marker exists
- [ ] sensitive confirmation UI
- [ ] category correction storage

### UI

- [x] Onboarding marker exists
- [x] Home marker exists
- [x] Detail marker exists
- [x] Settings marker exists
- [x] Permission denied/limited mode marker exists: `ImagePermissionCard`, `LimitedModeCard`
- [x] Retry/skipped actions marker exists
- [x] Markdown preview marker exists
- [ ] Drive open link

## 4. QA 실행 체크리스트

프로젝트 생성 후 QA가 최소 실행해야 할 명령:

```bash
./gradlew test
./gradlew assembleDebug
./gradlew lintDebug
```

가능한 환경에서 추가 실행:

```bash
./gradlew connectedDebugAndroidTest
```

수동/기기 QA:

- [ ] Android 13+ 실제 기기에서 이미지 권한 허용
- [ ] 앱 외부에서 스크린샷 촬영
- [ ] 10초 이내 Home에 queued/processing 표시
- [ ] OCR 완료 후 Markdown preview 확인
- [ ] Google Drive에 이미지/note.md/metadata.json 생성 확인
- [ ] 네트워크 OFF 후 failed_upload 상태 확인
- [ ] 네트워크 ON 후 재시도 성공 확인
- [ ] 같은 스크린샷 재스캔 시 중복 없음
- [ ] 민감정보 fixture는 업로드 전 확인 대기
- [ ] 자동 감지 OFF 상태에서는 신규 처리 없음

## 5. 현재 알려진 블로커

- Android Studio snap JBR, Android SDK, platform-tools, Gradle wrapper가 연결되었고 `testDebugUnitTest`, `assembleDebug`, `lintDebug`, `signingReport`가 통과했다. `MainActivity.kt`/`MarkdownGenerator`/Drive dependency compile-risk는 debug 빌드 기준 해소되었다.
- current job developer reports `26052714:58:53` and `26052715:00:12`는 접수되었고, developer lane static evidence는 현재 job 기준으로 존재한다. 남은 blocker는 Google Cloud OAuth Client 등록과 실제 device-OAuth screenshot-to-Drive E2E이다.
- `GoogleDriveUploaderStub`는 제거되었고 `UserGoogleDriveUploader`가 실제 Drive 업로드를 구현한다.
- Google Sign-In/OAuth account flow, `DriveScopes.DRIVE_FILE`, 계정 저장 처리, Drive uploader 구현이 추가되었다.
- Kotlin package와 Gradle `namespace`/`applicationId`는 `com.ponslink.capturebrain`으로 정렬되었다. 사용자 제공 후보 `com.ponslink.com.capturebrain`은 채택하지 않았다.
- ContentObserver 실등록/periodic scan/DataStore settings marker는 추가되었지만 screenshot-to-Drive end-to-end 흐름은 실기기에서 미검증이다.
- Google OAuth Client ID가 아직 없다. package name은 `com.ponslink.capturebrain`, debug SHA-1은 `81:62:C3:6B:50:FC:48:26:B3:3B:8F:A6:8A:30:71:F7:33:BA:8D:EB` 기준으로 맞춰야 한다.
- 실제 Android 기기/에뮬레이터 테스트가 아직 수행되지 않았다.
- AI API 공급자와 운영 방식이 확정되지 않았다.

## 6. README 갱신 조건

개발팀이 다음을 추가하면 README를 즉시 갱신해야 한다.

- 실제 package/applicationId
- 실제 Gradle 명령
- Google OAuth 설정 방법
- 샘플 이미지/fixture 경로
- 현재 구현된 단계와 미구현 단계
- 실행 가능한 QA 명령
