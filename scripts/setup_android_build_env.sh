#!/usr/bin/env bash
set -euo pipefail

TOOLS="$HOME/.local/capturebrain-android-tools"
SDK="$HOME/Android/Sdk"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_STUDIO_JBR="/snap/android-studio/current/jbr"

mkdir -p "$TOOLS" "$SDK" "$HOME/.local/bin"
cd "$TOOLS"

if [ -x "$ANDROID_STUDIO_JBR/bin/java" ]; then
  echo "Using Android Studio snap bundled JBR..."
  export JAVA_HOME="$ANDROID_STUDIO_JBR"
elif [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  echo "Using existing JAVA_HOME..."
else
  echo "ERROR: Android Studio snap JBR not found at $ANDROID_STUDIO_JBR and JAVA_HOME is not usable." >&2
  echo "Install Android Studio snap or export JAVA_HOME before running this script." >&2
  exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"
java -version

if [ ! -x "$TOOLS/gradle-8.10.2/bin/gradle" ]; then
  echo "Downloading Gradle 8.10.2..."
  rm -rf gradle-8.10.2 gradle-8.10.2-bin.zip
  curl -L --fail -o gradle-8.10.2-bin.zip 'https://services.gradle.org/distributions/gradle-8.10.2-bin.zip'
  unzip -oq gradle-8.10.2-bin.zip
fi

export PATH="$TOOLS/gradle-8.10.2/bin:$PATH"
gradle -v | head -20

if [ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "Downloading Android command line tools..."
  curl -L --fail -o cmdline-tools.zip 'https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip'
  rm -rf "$SDK/cmdline-tools"
  mkdir -p "$SDK/cmdline-tools/tmp"
  unzip -q cmdline-tools.zip -d "$SDK/cmdline-tools/tmp"
  mkdir -p "$SDK/cmdline-tools/latest"
  mv "$SDK/cmdline-tools/tmp/cmdline-tools"/* "$SDK/cmdline-tools/latest/"
  rm -rf "$SDK/cmdline-tools/tmp"
fi

export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
export PATH="$SDK/cmdline-tools/latest/bin:$SDK/platform-tools:$PATH"

echo "Accepting Android SDK licenses..."
yes | sdkmanager --licenses >/tmp/capturebrain-sdk-licenses.log || true

echo "Installing Android SDK packages..."
sdkmanager --install 'platform-tools' 'platforms;android-35' 'build-tools;35.0.0'

cd "$PROJECT_ROOT"
if [ ! -x ./gradlew ]; then
  echo "Generating Gradle wrapper..."
  gradle wrapper --gradle-version 8.10.2 --distribution-type bin
fi

cat > .capturebrain-android-env <<EOF
export JAVA_HOME="$JAVA_HOME"
export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
export PATH="$JAVA_HOME/bin:$TOOLS/gradle-8.10.2/bin:$SDK/cmdline-tools/latest/bin:$SDK/platform-tools:\$PATH"
EOF

source ./.capturebrain-android-env

echo "Environment ready. Versions:"
java -version
./gradlew --version | head -20

echo "Next commands:"
echo "  source ./.capturebrain-android-env"
echo "  ./gradlew test"
echo "  ./gradlew assembleDebug"
echo "  ./gradlew lintDebug"
echo "  ./gradlew signingReport"
