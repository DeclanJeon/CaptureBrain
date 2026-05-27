# CaptureBrain Google Drive / OAuth Setup

목적: CaptureBrain Android 앱이 개발자 서버/서비스계정 Drive가 아니라 사용자 본인의 Google Drive에 저장하도록 설정·검증하는 절차를 정의한다.

## 1. 현재 결정/미결정 값

| 항목 | 현재 값/상태 |
|---|---|
| 방식 | Android Google Sign-In / OAuth per user |
| Drive 권한 | `https://www.googleapis.com/auth/drive.file` |
| 테스트 사용자 | `syas0301@gmail.com` |
| 지원/개발자 이메일 | `ponslink@gmail.com` |
| Kotlin package | 현재 소스는 `com.ponslink.capturebrain` |
| Gradle namespace/applicationId | `app/build.gradle.kts`를 `com.ponslink.capturebrain`으로 정렬 완료 |
| 사용자 제안 package | `com.ponslink.com.capturebrain` 가능성 언급됨. 최종 확정 전까지 OAuth Client 생성 금지 |
| 빌드/서명 fingerprint | 현재 환경에 `java`, `gradle`, `./gradlew`, `keytool`이 없어 SHA-1 산출 불가 |

권장 정리안: Kotlin package, Gradle `namespace`, Gradle `applicationId`, Google Cloud Android OAuth package name을 하나로 통일한다. 현재 소스 경로와 package 선언 기준으로는 `com.ponslink.capturebrain`이 가장 자연스럽다. 단, 사용자가 최종적으로 `com.ponslink.com.capturebrain`을 원하면 코드 package와 Gradle 설정을 모두 함께 변경해야 한다.

## 2. Google Cloud Console 설정

1. Google Cloud Project를 생성하거나 기존 프로젝트를 선택한다.
2. API & Services → Library에서 Google Drive API를 활성화한다.
3. OAuth consent screen을 구성한다.
   - User type: 개발 단계에서는 External + Testing 또는 내부 계정 정책에 맞게 선택
   - App name: CaptureBrain
   - User support email: `ponslink@gmail.com`
   - Developer contact email: `ponslink@gmail.com`
   - Test users: `syas0301@gmail.com`
4. Scopes에는 최소 권한만 추가한다.
   - `https://www.googleapis.com/auth/drive.file`
   - 전체 Drive scope(`drive`, `drive.readonly`)는 사용하지 않는다.
5. Credentials → Create Credentials → OAuth client ID → Android를 선택한다.
6. Android OAuth Client에 아래 값을 넣는다.
   - Package name: 최종 확정된 `applicationId`
   - SHA-1 certificate fingerprint: debug/release keystore에서 산출한 값

## 3. SHA-1 fingerprint 산출

JDK/Gradle wrapper가 준비된 개발 머신에서 실행한다.

```bash
./gradlew signingReport
```

또는 debug keystore 기준:

```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android
```

출력의 SHA1 값을 Google Cloud Android OAuth Client에 등록한다. release 빌드도 배포할 경우 release keystore SHA-1을 별도로 등록해야 한다.

## 4. Android 구현 요구사항

개발자가 구현해야 하는 최소 항목:

1. package/applicationId 정렬
   - `app/build.gradle.kts`의 `namespace`와 `applicationId`
   - Kotlin `package` 선언과 디렉터리
   - AndroidManifest activity 해석
   - Google Cloud Android OAuth Client package name
2. 사용자 계정 연결 UI
   - Settings 또는 Onboarding에서 Google 계정 연결/해제
   - 테스트 계정 `syas0301@gmail.com`로 smoke 가능해야 함
3. 사용자 OAuth 기반 Drive client
   - `GoogleDriveUploaderStub` 제거 완료. `UserGoogleDriveUploader`가 실제 사용자 Drive 업로드를 담당
   - `DriveAccountStore`는 선택된 account email/root folder name만 저장하고 OAuth token은 직접 저장하지 않음
   - `DriveScopes.DRIVE_FILE`만 사용
   - 서비스 계정 JSON, refresh token, client secret을 앱/저장소에 포함하지 않음
4. 안전한 계정/토큰 처리
   - Google Identity Services / Google Play services auth 계정 객체를 사용
   - 토큰 문자열을 Room/DataStore에 평문 저장하지 않음
   - 저장이 필요하면 계정 식별자/이메일 등 최소 상태만 저장
5. WorkManager 네트워크 제약
   - Drive 업로드 worker/request는 `NetworkType.CONNECTED`를 기본 조건으로 둠
   - Wi-Fi only 설정이 켜지면 `NetworkType.UNMETERED`로 전환
6. Drive 파일 생성
   - root folder `CaptureBrain` ensure
   - category/date/capture folder ensure
   - 원본 이미지, `note.md`, `metadata.json` 업로드
   - 생성된 Drive file/folder id를 Room에 저장해 중복 업로드를 방지

## 5. 보안 금지사항

- 서비스 계정으로 개발자/서버 Drive에 업로드하지 않는다.
- 앱에 service account JSON, client secret, refresh token, API key를 하드코딩하지 않는다.
- OAuth scope를 전체 Drive 권한으로 확대하지 않는다.
- OCR 원문, 민감 이미지 내용, OAuth token, Drive file id 전체를 로그에 남기지 않는다.
- 사용자가 연결 해제하면 로컬 계정 상태를 삭제하고 이후 업로드를 중단한다.

## 6. 로컬/수동 검증 체크리스트

현재 이 AgentDock 실행 환경에서는 JDK/Gradle/keytool이 없어 아래 명령을 실행할 수 없다. 가능한 개발 머신 또는 CI에서 실행한다.

```bash
./gradlew test
./gradlew assembleDebug
./gradlew lintDebug
./gradlew signingReport
```

기기 smoke:

1. 앱 설치
2. Google 계정 `syas0301@gmail.com` 연결
3. Drive 권한 consent에서 `drive.file` 범위 확인
4. 테스트 스크린샷 1장 촬영 또는 수동 import
5. OCR/Markdown 생성 확인
6. Google Drive `CaptureBrain/.../{capture_slug}/`에 원본 이미지, `note.md`, `metadata.json` 생성 확인
7. 같은 스크린샷 재처리 시 중복 파일이 생기지 않는지 확인
8. 네트워크 OFF 상태에서 failed_upload/재시도 큐 확인
9. 네트워크 ON 후 재시도 성공 확인
10. 계정 연결 해제 후 신규 업로드가 차단되는지 확인

## 7. 현재 블로커

- `GoogleDriveUploaderStub`는 제거되었고 `UserGoogleDriveUploader`가 실제 업로드를 수행한다.
- `CaptureProcessWorker`는 연결된 계정이 있으면 `UserGoogleDriveUploader`, 없으면 `UnconnectedDriveUploader`를 사용한다.
- `DriveScopes.DRIVE_FILE`, Google Sign-In/OAuth account flow, 계정 저장 marker가 코드에 있다.
- WorkManager upload request에 `NetworkType.CONNECTED` 제약이 있다.
- `app/build.gradle.kts`의 `namespace`/`applicationId`와 Kotlin package는 `com.ponslink.capturebrain`으로 정렬되었다.
- SHA-1 fingerprint는 JDK/keytool/Gradle wrapper가 준비된 환경에서 산출해야 한다.
