#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

say() { printf '\n== %s ==\n' "$1"; }
need() { if command -v "$1" >/dev/null 2>&1; then echo "OK $1: $(command -v "$1")"; else echo "MISSING $1"; fi; }

say "Tool availability"
need java
need keytool
need gradle
if [ -x ./gradlew ]; then echo "OK gradlew: ./gradlew"; else echo "MISSING gradlew"; fi
need adb

say "Package/applicationId"
if [ -f app/build.gradle.kts ]; then
  grep -E 'namespace\s*=|applicationId\s*=' app/build.gradle.kts || true
else
  echo "MISSING app/build.gradle.kts"
fi

say "Kotlin package declarations"
python3 - <<'PY'
from pathlib import Path
pkgs={}
for p in Path('app/src/main/java').rglob('*.kt'):
    for line in p.read_text(encoding='utf-8').splitlines():
        if line.startswith('package '):
            pkgs.setdefault(line.split()[1],0)
            pkgs[line.split()[1]] += 1
            break
for pkg,n in sorted(pkgs.items()):
    print(f'{pkg}: {n}')
PY

say "Drive/OAuth implementation markers"
python3 - <<'PY'
from pathlib import Path
text='\n'.join(p.read_text(encoding='utf-8') for p in Path('app/src/main/java').rglob('*.kt'))
markers=[
 'GoogleAccountCredential',
 'DriveScopes.DRIVE_FILE',
 'GoogleSignIn',
 'GoogleSignInOptions',
 'DriveAccountStore',
 'UserGoogleDriveUploader',
 'UnconnectedDriveUploader',
 'NetworkType.CONNECTED',
 'GoogleDriveUploaderStub()',
 'service_account',
 'client_secret',
 'refresh_token',
 'private_key',
 'access_token',
]
for m in markers:
    print(f'{m}: {"FOUND" if m in text else "absent"}')
# Exact broad Drive scope check. Avoid false positives from DriveScopes.DRIVE_FILE.
import re
broad_drive = bool(re.search(r'DriveScopes\.DRIVE(?![_A-Z0-9])', text))
print(f'DriveScopes.DRIVE (broad exact): {"FOUND" if broad_drive else "absent"}')
PY

say "Suggested build checks"
cat <<'EOF'
When Java/Gradle wrapper are available, run:
  ./gradlew test
  ./gradlew assembleDebug
  ./gradlew lintDebug
  ./gradlew signingReport

Device smoke after OAuth Client package/SHA-1 is registered:
  1. Install debug APK
  2. Connect syas0301@gmail.com
  3. Confirm consent scope is drive.file
  4. Take/import one screenshot
  5. Confirm Google Drive CaptureBrain folder has image, note.md, metadata.json
  6. Retry duplicate and offline/online flows
  7. Disconnect account and confirm uploads are blocked
EOF
