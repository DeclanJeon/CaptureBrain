# CaptureBrain UI image generation prompt

생성 일시: 2026-05-28
프로젝트: CaptureBrain Android
목표: 현재 Compose UI를 고급 Android 생산성 앱 톤으로 재설계하기 위한 UI reference 이미지 생성

## Visual direction

- Product: CaptureBrain — Android screenshot-to-second-brain automation app
- Tone: modern Korean productivity SaaS, calm high-tech, privacy-first
- Platform: Android / Material 3 inspired
- Palette: deep navy background, electric blue primary, mint success/accent, soft glass-like cards
- Typography: Korean-first, large readable mobile text
- Must show:
  - user-owned Google Drive, not service account/developer server upload
  - `drive.file` 최소 권한
  - no screenshot upload to CaptureBrain server
  - screenshot automation switch
  - queue status cards
  - recent captures list
  - OCR/Markdown preview
  - failed retry/manual scan controls
  - Wi-Fi-only upload and sensitive-confirm settings

## Original Imagen-style board prompt

```txt
Design a premium Android mobile app UI for "CaptureBrain", a screenshot-to-second-brain automation app.
Style: modern Korean productivity SaaS, calm but high-tech, Material 3 inspired, clean cards, soft gradients, deep navy / electric blue / mint accent, large readable Korean text, privacy-first Google Drive workflow.
Screens: onboarding, home dashboard, capture detail with OCR and Markdown preview, settings with Google Drive account and automation toggles.
Important UI elements: screenshot automation switch, queue status cards, Google Drive connected account, sensitive information warning, recent captures list, Markdown preview, retry failed uploads, manual scan, Wi-Fi-only upload.
Output: four phone screens in one board, realistic Android proportions, production-ready visual hierarchy, no lorem ipsum, Korean labels.
```

## Per-screen prompts used

### 01 Onboarding

```txt
Premium Android mobile app UI mockup for "CaptureBrain", screen 1 of 4: onboarding / setup. A screenshot-to-second-brain automation app for Korean users. Modern Korean productivity SaaS aesthetic, Material 3 inspired, calm high-tech. Deep navy background (#07111f), soft glass cards, electric blue and mint accents, subtle gradient glow, large readable Korean UI labels. Show a hero title in Korean: "스크린샷이 지식 노트로" and subtitle about automatic OCR, Markdown, Google Drive. Include three setup checklist cards: "이미지 권한", "자동 감지", "Google Drive 연결". Show privacy-first badge: "서버 업로드 없음 · 내 Drive 저장". Bottom CTA button: "자동 처리 시작". Realistic Android phone screen, 9:20 ratio, production-ready visual hierarchy, not generic AI purple, no lorem ipsum, no service account wording.
```

### 02 Home dashboard

```txt
Premium Android mobile app UI mockup for "CaptureBrain", screen 2 of 4: home dashboard. Korean productivity Android app, Material 3 inspired, deep navy / electric blue / mint accent, clean cards, rounded but disciplined, high readability. Show top app title CaptureBrain and connected Google account syas0301@gmail.com. Main automation card with switch ON: "자동 감지 ON" and status "새 스크린샷을 Markdown으로 변환 중". Four metric cards: "오늘 완료 4", "대기 0", "실패 0", "확인 필요 0". Buttons: "최근 스크린샷 스캔", "실패 재시도". Recent captures list with status pills "완료", Drive icon, Markdown icon. Privacy strip: "내 Google Drive에만 저장". Realistic Android phone screen, 9:20 ratio, Korean labels, no lorem ipsum, production dashboard style.
```

### 03 Capture detail

```txt
Premium Android mobile app UI mockup for "CaptureBrain", screen 3 of 4: capture detail. A Korean Android app that converts screenshots to searchable Markdown notes. Material 3 inspired, calm dark navy surface, electric blue and mint accents, polished SaaS mobile UI. Layout: top screenshot preview card with a small captured web page thumbnail, title "전남대 도서관 소식지" and status pill "Drive 업로드 완료". Show metadata chips: "Chrome", "2026.05.28", "OCR 96%". Middle card: "추출 텍스트" with Korean OCR text lines. Lower card: "Markdown 미리보기" with code-like markdown preview, folder path "CaptureBrain/Research/Chrome" and two file rows: image JPG and note MD. Include action buttons "Drive에서 열기" and "다시 처리". Realistic Android phone screen, 9:20 ratio, no lorem ipsum, readable Korean UI.
```

### 04 Settings and privacy

```txt
Premium Android mobile app UI mockup for "CaptureBrain", screen 4 of 4: settings and privacy controls. Korean Android productivity app, Material 3 inspired, deep navy background, soft elevated cards, electric blue and mint accents, readable Korean typography. Show section "Google Drive 연결" with account syas0301@gmail.com, badge "drive.file 최소 권한", button "연결 해제". Show settings rows with switches: "Wi‑Fi에서만 업로드", "민감정보 확인 후 업로드", "온디바이스 OCR 우선", "자동 폴더 분류". Show warning card "민감한 캡처는 업로드 전 확인" with shield icon. Show Markdown template picker and root folder "CaptureBrain". Footer text: "CaptureBrain 서버에는 스크린샷을 업로드하지 않습니다." Realistic Android phone screen, 9:20 ratio, no service account wording, no lorem ipsum.
```

## Environment note

The configured Hermes image generation backend was unavailable because `FAL_KEY` is not set. To keep the UI design work moving, the reference images in this directory were generated as deterministic high-fidelity PNG mockups with a local Python/Pillow generator using the same visual direction and screen requirements.
