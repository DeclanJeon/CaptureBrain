# CaptureBrain UI Reference Work Order

작성 목적: 사용자 요청의 “codex imagen 스킬로 UI 레퍼런스를 뽑은 다음 그 이미지 UI를 그대로 재현” 조건을 구현팀과 QA가 검증 가능한 작업 단위로 만든다.

## 1. 현재 상태

현재 `app/src/main/java/com/ponslink/capturebrain/ui/CaptureBrainApp.kt`에는 Material 3 기반의 초기 Compose 화면이 있다.

존재하는 화면:

- Onboarding
- Home
- Capture Detail
- Settings
- Google Drive connection card
- Privacy warning card
- Queue summary cards
- Markdown preview/text cards

하지만 현재 UI는 아직 “이미지 레퍼런스 기반 재현”이라고 볼 수 없다. 레퍼런스 이미지 파일, 생성 prompt, 선택된 visual direction, 적용 diff, QA 비교 기준이 저장소에 없다.

## 2. UI 레퍼런스 생성 요구사항

담당: frontend-developer

필수 산출물:

1. `docs/ui_reference/capturebrain_imagen_prompt.md`
2. `docs/ui_reference/capturebrain_reference_result.md`
3. reference image 파일 또는 접근 가능한 경로/URL 기록
4. Compose 적용 요약
5. QA 비교 체크리스트

권장 prompt 방향:

```txt
Design a premium Android mobile app UI for "CaptureBrain", a screenshot-to-second-brain automation app.
Style: modern Korean productivity SaaS, calm but high-tech, Material 3 inspired, clean cards, soft gradients, deep navy / electric blue / mint accent, large readable Korean text, privacy-first Google Drive workflow.
Screens: onboarding, home dashboard, capture detail with OCR and Markdown preview, settings with Google Drive account and automation toggles.
Important UI elements: screenshot automation switch, queue status cards, Google Drive connected account, sensitive information warning, recent captures list, Markdown preview, retry failed uploads, manual scan, Wi-Fi-only upload.
Output: four phone screens in one board, realistic Android proportions, production-ready visual hierarchy, no lorem ipsum, Korean labels.
```

주의:

- service account/developer server upload 느낌을 주면 안 된다. “사용자 Google Drive”를 명확히 보여야 한다.
- 개인정보 보호/민감정보 확인이 시각적으로 드러나야 한다.
- automation ON/OFF, failed/retry, pending sensitive states가 있어야 한다.
- screenshot 원본과 Markdown 결과가 한 화면에서 연결되어야 한다.

## 3. Compose 재현 체크리스트

frontend-developer가 reference를 반영할 때 최소 확인할 항목:

- [ ] Onboarding hero가 reference의 tone/color/spacing과 일치
- [ ] Home dashboard에 queue cards, automation toggle, recent captures list 반영
- [ ] Detail screen에 screenshot preview, OCR original text, summary, Markdown preview, Drive path 반영
- [ ] Settings에 Google Drive account connect/disconnect, root folder, Wi-Fi only, AI analysis, sensitive confirm, Markdown template 반영
- [ ] Empty/loading/error/permission denied 상태 디자인 추가
- [ ] Sensitive pending 상태가 Drive upload 전 확인 흐름으로 명확히 표현
- [ ] `drive.file` 최소 권한과 “CaptureBrain 서버에 업로드하지 않음” 문구 유지
- [ ] UI label은 Korean-first, Android readable typography 사용
- [ ] magic sample data만 보여주는 상태에서 실제 ViewModel/Repository 연결 TODO를 명확히 분리

## 4. QA 검증 기준

QA는 다음을 확인한다.

1. `docs/ui_reference/` 산출물이 존재하는가?
2. reference prompt와 결과가 CaptureBrain PRD/MVP와 맞는가?
3. Compose UI가 reference의 주요 구조를 반영하는가?
4. 개인정보/Drive/user-owned copy가 UI에서 빠지지 않았는가?
5. Material/Compose build가 가능한 환경에서는 `./gradlew test`, `./gradlew assembleDebug`, `./gradlew lintDebug`가 통과하는가?
6. build 도구가 없으면 static scan으로 `CaptureBrainApp.kt`의 화면 marker와 reference doc을 확인하고 build/device는 risk로 기록한다.

## 5. 현재 job handoff

현 시점 tech-writer 판정:

- UI reference generation: Not started in repository
- Current Compose UI: In progress scaffold
- Required next lane: frontend-developer must create reference artifact and patch Compose UI against it
- QA blocker status: valid until frontend-developer report includes reference evidence
