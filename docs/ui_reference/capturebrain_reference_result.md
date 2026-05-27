# CaptureBrain UI reference result

생성 일시: 2026-05-28

## Result files

Reference board:

- `/home/declan/Documents/Develop/Auto_ScreenShot/docs/ui_reference/images/capturebrain_ui_reference_board.png`

Individual mobile screens:

- `/home/declan/Documents/Develop/Auto_ScreenShot/docs/ui_reference/images/01_onboarding.png`
- `/home/declan/Documents/Develop/Auto_ScreenShot/docs/ui_reference/images/02_home_dashboard.png`
- `/home/declan/Documents/Develop/Auto_ScreenShot/docs/ui_reference/images/03_capture_detail.png`
- `/home/declan/Documents/Develop/Auto_ScreenShot/docs/ui_reference/images/04_settings_privacy.png`

Generator script:

- `/home/declan/Documents/Develop/Auto_ScreenShot/scripts/generate_capturebrain_ui_refs.py`

Prompt source:

- `/home/declan/Documents/Develop/Auto_ScreenShot/docs/ui_reference/capturebrain_imagen_prompt.md`

## Selected visual direction

`Linear-inspired dark productivity dashboard + Material 3 Android mobile`.

Core choices:

- Deep navy base instead of generic purple gradient
- Electric blue primary CTA
- Mint status/accent for active automation and completed upload
- Thin blue-gray borders and elevated card surfaces
- Korean-first readable text hierarchy
- Product-specific privacy copy, not generic placeholder text

## Screen intent

### 01 Onboarding

Purpose: explain the value proposition and required setup steps before the user starts automation.

Key elements:

- Hero: `스크린샷이 지식 노트로`
- Setup cards: image permission, automatic detection, Google Drive connection
- Privacy strip: `서버 업로드 없음 · 내 Google Drive 저장`
- CTA: `자동 처리 시작`

### 02 Home dashboard

Purpose: daily operation dashboard for automatic screenshot processing.

Key elements:

- Automation switch ON
- Queue metrics: completed, pending, failed, sensitive-review needed
- Manual scan and retry failed buttons
- Recent captures list with completed status
- User-owned Drive path hint

### 03 Capture detail

Purpose: connect the original screenshot, extracted text, Markdown result, and Drive output in one review screen.

Key elements:

- Screenshot preview
- Status: `Drive 업로드 완료`
- OCR confidence and source app chips
- Extracted text card
- Markdown preview card
- Drive folder path
- Actions: open in Drive, reprocess

### 04 Settings and privacy

Purpose: make account ownership, minimum permissions, upload policy, and sensitive-data controls explicit.

Key elements:

- Connected account: `syas0301@gmail.com`
- `drive.file 최소 권한`
- User-owned Drive copy
- Wi-Fi-only upload
- Sensitive-confirm-before-upload
- On-device OCR priority
- Automatic folder classification
- Server-no-upload footer copy

## QA comparison checklist

- [x] UI shows CaptureBrain as screenshot-to-second-brain automation app
- [x] UI uses Korean-first labels
- [x] UI communicates user-owned Google Drive, not service account/developer Drive
- [x] UI includes `drive.file` minimum permission concept
- [x] UI includes no-server-upload privacy copy
- [x] UI includes automation ON/OFF
- [x] UI includes queue status cards
- [x] UI includes recent captures list
- [x] UI includes manual scan and failed retry controls
- [x] UI includes OCR/Markdown detail preview
- [x] UI includes sensitive-confirm setting
- [x] UI includes Wi-Fi-only upload setting
- [x] UI avoids lorem ipsum and generic AI purple aesthetic

## Notes for Compose implementation

Suggested implementation direction for `CaptureBrainApp.kt`:

1. Replace the current flat Material cards with layered `CaptureSurfaceCard` styles:
   - base: deep navy
   - elevated card: slightly lighter navy
   - border: blue-gray with low alpha
   - primary action: electric blue
   - active/success: mint

2. Add reusable components:
   - `MetricCard`
   - `StatusPill`
   - `PrivacyStrip`
   - `DriveAccountCard`
   - `CapturePreviewCard`
   - `MarkdownPreviewCard`
   - `SettingsSwitchRow`

3. Preserve functional requirements:
   - no broad Drive scope wording
   - no service account wording
   - `CaptureBrain 서버에는 스크린샷을 업로드하지 않습니다.` remains visible in onboarding/settings
   - failed retry and manual scan stay prominent

4. Add missing product states later:
   - permission denied
   - Drive disconnected
   - upload failed with user-recoverable consent
   - sensitive pending confirmation
   - empty recent captures
   - WorkManager processing state

## Environment note

Direct Hermes image generation failed because the image backend is not configured in this session:

- missing `FAL_KEY`

Therefore, the committed references are local PNG mockups generated from the same Imagen-style prompts with Pillow. They are deterministic and suitable for implementation reference, but not diffusion-model output.
