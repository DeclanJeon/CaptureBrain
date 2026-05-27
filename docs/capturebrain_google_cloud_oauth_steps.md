# Google Cloud OAuth 설정 가이드 - CaptureBrain

확정값:
- Android package/applicationId: `com.ponslink.capturebrain`
- 앱 이름: `CaptureBrain`
- Drive scope: `https://www.googleapis.com/auth/drive.file`
- Support email / Developer contact email: `ponslink@gmail.com`
- Test user: `syas0301@gmail.com`

## 1. OAuth consent screen 설정

Google Cloud Console에서 현재 프로젝트 선택 후:

1. APIs & Services → OAuth consent screen
2. User Type:
   - 개인/외부 테스트면 `External`
   - 조직 내부 앱이면 정책에 맞게 선택
3. Publishing status는 개발 중이면 `Testing`
4. App information:
   - App name: `CaptureBrain`
   - User support email: `ponslink@gmail.com`
5. Developer contact information:
   - `ponslink@gmail.com`
6. Scopes:
   - `https://www.googleapis.com/auth/drive.file` 만 추가
   - `.../auth/drive` 전체 권한은 추가하지 말 것
7. Test users:
   - `syas0301@gmail.com` 추가

## 2. Android OAuth Client 생성

SHA-1 fingerprint가 필요하므로 먼저 이 프로젝트에서 아래 명령을 실행해야 함:

```bash
source ./.capturebrain-android-env
./gradlew signingReport
```

출력에서 `Variant: debug`, `Config: debug` 쪽의 `SHA1:` 값을 복사.

그 다음 Google Cloud Console:

1. APIs & Services → Credentials
2. Create Credentials → OAuth client ID
3. Application type: Android
4. Name: `CaptureBrain Android Debug`
5. Package name: `com.ponslink.capturebrain`
6. SHA-1 certificate fingerprint: `./gradlew signingReport`에서 나온 debug SHA1
7. Create

Release 빌드/Play 배포 시에는 release keystore SHA-1 또는 Play App Signing SHA-1로 Android OAuth Client를 별도 생성해야 함.

## 3. 실기기 smoke

OAuth Client 생성 후:

1. `./gradlew assembleDebug`
2. APK 설치 또는 Android Studio Run
3. 앱에서 Google Drive 연결
4. `syas0301@gmail.com` 선택
5. consent 화면에서 Drive 파일 생성/관리 최소 권한(`drive.file`) 확인
6. 스크린샷 1장 촬영 또는 수동 import
7. Google Drive에 `CaptureBrain/...` 폴더와 원본 이미지, `note.md`, `metadata.json` 생성 확인
8. 같은 스크린샷 재처리 시 중복 생성 없는지 확인
9. 네트워크 OFF/ON 재시도 확인
10. 계정 연결 해제 후 신규 업로드 차단 확인
