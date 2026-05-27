# Release AAB 빌드 지침 - 캡처브레인

## 현재 상태

- `./gradlew bundleRelease` 빌드는 성공합니다.
- 단, 업로드 키스토어 환경변수를 설정하지 않으면 생성된 AAB는 unsigned 상태라 Play Console 업로드용으로 쓰면 안 됩니다.
- `app/build.gradle.kts`에는 환경변수 기반 release signing 설정을 추가해 두었습니다.

## 업로드 키스토어 생성, 최초 1회

키스토어 파일은 저장소에 커밋하지 마세요.

```bash
mkdir -p ~/.android/keystores
/snap/android-studio/current/jbr/bin/keytool \
  -genkeypair \
  -v \
  -keystore ~/.android/keystores/capturebrain-upload.jks \
  -alias capturebrain-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

비밀번호는 폰스가 직접 정해야 합니다. 채팅이나 코드에 남기지 마세요.

## 릴리즈 SHA 확인

```bash
/snap/android-studio/current/jbr/bin/keytool \
  -list -v \
  -keystore ~/.android/keystores/capturebrain-upload.jks \
  -alias capturebrain-upload
```

출력되는 SHA-1을 Google Cloud Console의 Android OAuth Client에 등록해야 실제 Play 설치본에서 Google Drive 로그인이 됩니다.

## 서명 환경변수 설정

예시:

```bash
export CAPTUREBRAIN_UPLOAD_STORE_FILE="$HOME/.android/keystores/capturebrain-upload.jks"
export CAPTUREBRAIN_UPLOAD_STORE_PASSWORD="직접입력"
export CAPTUREBRAIN_UPLOAD_KEY_ALIAS="capturebrain-upload"
export CAPTUREBRAIN_UPLOAD_KEY_PASSWORD="직접입력"
```

## 업로드용 AAB 빌드

```bash
JAVA_HOME=/snap/android-studio/current/jbr ./gradlew clean bundleRelease --no-daemon --no-watch-fs --max-workers=1
```

결과물:

```text
app/build/outputs/bundle/release/app-release.aab
```

## 서명 확인

```bash
/snap/android-studio/current/jbr/bin/jarsigner \
  -verify -verbose -certs \
  app/build/outputs/bundle/release/app-release.aab
```

`jar is unsigned`가 나오면 Play Console 업로드용이 아닙니다. 환경변수와 키스토어를 다시 확인하세요.
