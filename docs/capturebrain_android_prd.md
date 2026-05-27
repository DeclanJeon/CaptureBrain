# CaptureBrain PRD
## Android Screenshot-to-Second-Brain App

문서 버전: v1.0  
작성일: 2026-05-26  
대상 플랫폼: Android  
문서 목적: 안드로이드 스크린샷 자동 수집, OCR, Markdown 변환, 클라우드 드라이브 저장 기반 세컨드브레인 앱의 제품 요구사항 정의

---

# 1. 제품 개요

## 1.1 한 줄 정의

**안드로이드에서 찍은 스크린샷을 자동으로 수집하고, 이미지 내용을 원문 보존형 Markdown으로 변환해 클라우드 드라이브에 지식 폴더로 정리해주는 세컨드브레인 앱**

## 1.2 제품 컨셉

사용자는 웹, 앱, SNS, 메신저, 문서, 쇼핑몰, 강의 자료, 코드, 오류 메시지, 아이디어 등을 스크린샷으로 저장한다.  
하지만 스크린샷은 대부분 갤러리에 쌓인 뒤 다시 찾기 어렵다.

CaptureBrain은 스크린샷을 자동으로 감지하고, 이미지의 내용을 OCR과 AI 분석으로 읽어 Markdown 노트로 변환한 뒤, Google Drive 같은 클라우드 드라이브에 적절한 폴더 구조로 저장한다.

즉, 사용자가 스크린샷을 찍는 행위 자체가 지식 수집 행위가 된다.

---

# 2. 문제 정의

## 2.1 사용자가 겪는 문제

사용자는 유용한 정보를 발견하면 스크린샷을 찍지만, 이후 다음 문제가 발생한다.

1. 스크린샷이 너무 많이 쌓여 찾기 어렵다.
2. 이미지 안의 텍스트를 검색하기 어렵다.
3. 어느 앱에서 찍었는지, 어떤 주제인지 기억나지 않는다.
4. 좋은 내용이 있어도 Notion, Obsidian, Drive 등에 따로 옮기지 않는다.
5. 이미지 내용이 텍스트화되지 않아 AI 요약, 검색, 재활용이 어렵다.
6. 스크린샷은 많지만 실제 지식 저장소는 만들어지지 않는다.

## 2.2 기존 방식의 한계

| 방식 | 한계 |
|---|---|
| 갤러리 | 검색, 분류, 원문 추출이 약함 |
| Google Photos | 이미지 검색은 가능하지만 Markdown 지식화가 약함 |
| Notion 수동 저장 | 귀찮고 지속성이 낮음 |
| Obsidian 수동 정리 | 자동화가 부족함 |
| OCR 앱 | 캡처 후 수동 실행이 필요함 |
| 클라우드 드라이브 | 폴더 구조는 있지만 내용 이해가 없음 |

---

# 3. 제품 목표

## 3.1 전체 목표

CaptureBrain의 목표는 다음과 같다.

1. 사용자가 스크린샷을 찍으면 자동으로 감지한다.
2. 새 스크린샷 이미지를 안전하게 읽는다.
3. 이미지 내용을 OCR 및 AI 비전 분석으로 추출한다.
4. 원문을 최대한 보존한 Markdown 파일을 생성한다.
5. 이미지와 Markdown 파일을 함께 클라우드 드라이브에 저장한다.
6. 내용 기반으로 적절한 디렉토리를 자동 생성한다.
7. 사용자가 나중에 검색, 회고, 재활용할 수 있는 세컨드브레인 구조를 만든다.

## 3.2 MVP 목표

MVP에서는 완전한 지식관리 앱을 만들지 않는다.  
먼저 아래 핵심 흐름을 검증한다.

```txt
스크린샷 자동 감지 → OCR → Markdown 생성 → Google Drive 저장 → 폴더 자동 분류
```

---

# 4. 핵심 사용자

## 4.1 1차 타깃

| 사용자 | 사용 상황 |
|---|---|
| 개발자 | 에러 메시지, 코드, 문서, GitHub, Stack Overflow 캡처 |
| 기획자/창업가 | 경쟁 서비스, 가격 정책, UI 레퍼런스 캡처 |
| 학생/학습자 | 강의 자료, 문제, 개념 설명 캡처 |
| 리서처 | 웹 문서, 논문, 기사, SNS 인사이트 저장 |
| 콘텐츠 제작자 | 아이디어, 문장, 이미지 레퍼런스 수집 |

## 4.2 사용자 페르소나

### Persona A: 개발자

- 여러 앱과 웹페이지에서 정보를 자주 캡처한다.
- 나중에 다시 찾으려 하지만 갤러리에서 못 찾는다.
- Obsidian이나 Markdown 기반 지식 관리를 선호한다.
- 이미지 원본과 텍스트 추출본을 함께 보관하고 싶다.
- 자동화된 세컨드브레인이 필요하다.

### Persona B: 학습자

- 유튜브 강의, PDF, 블로그, 문제풀이 화면을 캡처한다.
- OCR로 텍스트를 추출하고 싶다.
- 과목별로 자동 정리되면 좋겠다.
- 복습할 때 Markdown 요약을 보고 싶다.

### Persona C: 1인 창업가

- 경쟁 서비스 UI, 가격표, 랜딩페이지, 광고 문구를 자주 캡처한다.
- 서비스명, 가격, 핵심 문구, CTA 등을 자동으로 정리하고 싶다.
- 나중에 사업 아이디어 자료로 재활용하고 싶다.

---

# 5. 핵심 가치 제안

| 가치 | 설명 |
|---|---|
| 자동화 | 스크린샷만 찍으면 정리까지 자동 수행 |
| 원문 보존 | OCR 결과를 최대한 원문 형태로 Markdown화 |
| 검색 가능 | 이미지 속 텍스트를 검색 가능한 파일로 변환 |
| 클라우드 백업 | 이미지와 Markdown을 클라우드 드라이브에 저장 |
| 지식화 | 단순 이미지 저장이 아니라 노트화 |
| 분류 자동화 | 주제별 폴더 자동 생성 |
| 로컬 우선 | 민감한 캡처는 로컬 처리 옵션 제공 가능 |

---

# 6. 제품 범위

## 6.1 MVP 포함 범위

### 필수 기능

1. 스크린샷 자동 감지
2. 신규 스크린샷 이미지 읽기
3. OCR 텍스트 추출
4. 이미지 내용 AI 분석
5. Markdown 파일 생성
6. Google Drive 연동
7. 자동 폴더 생성
8. 이미지 원본 업로드
9. Markdown 파일 업로드
10. 처리 이력 관리
11. 실패한 항목 재시도
12. 사용자가 자동화 ON/OFF 가능

## 6.2 MVP 제외 범위

1. iOS 앱
2. 데스크톱 앱
3. 팀 협업 기능
4. 웹 대시보드
5. Obsidian 실시간 동기화
6. Notion API 연동
7. 완전한 로컬 LLM 처리
8. 캡처 전 화면 맥락 분석
9. 브라우저 확장 프로그램
10. 고급 RAG 검색

---

# 7. 주요 사용자 시나리오

## 7.1 기본 사용 흐름

1. 사용자가 앱을 설치한다.
2. Google 계정으로 로그인한다.
3. Google Drive 접근 권한을 허용한다.
4. 스크린샷 감지 권한 및 이미지 접근 권한을 설정한다.
5. 사용자가 다른 앱에서 스크린샷을 찍는다.
6. CaptureBrain이 새 스크린샷을 감지한다.
7. 앱이 이미지를 분석한다.
8. OCR 텍스트를 추출한다.
9. 이미지 내용을 분류한다.
10. 적절한 폴더명을 생성한다.
11. 원본 이미지와 Markdown 파일을 Google Drive에 저장한다.
12. 사용자는 나중에 Drive에서 Markdown 파일을 검색하고 읽는다.

## 7.2 저장 예시

```txt
Google Drive/
  CaptureBrain/
    Learning/
      AI/
        2026/
          05/
            2026-05-26_YouTube_AI-Agent-Architecture/
              screenshot.png
              note.md
              metadata.json
```

## 7.3 Markdown 예시

```md
# YouTube AI Agent Architecture

- Captured at: 2026-05-26 14:32
- Source app: YouTube
- Category: Learning / AI
- Confidence: 0.86

## Extracted Text

AI Agent Architecture
- Planner
- Memory
- Tool Use
- Reflection
- Execution Loop

## Preserved Layout

[화면 상단]
AI Agent Architecture

[중앙 다이어그램]
Planner → Tool Use → Execution → Reflection

## Summary

이 이미지는 AI Agent의 기본 구조를 설명하는 강의 화면으로 보인다. 핵심 요소는 Planner, Memory, Tool Use, Reflection이다.

## Tags

#AI #Agent #Architecture #Learning #YouTube
```

---

# 8. 기능 요구사항

## 8.1 스크린샷 자동 감지

### 설명

앱은 사용자가 새 스크린샷을 찍었을 때 이를 자동으로 감지해야 한다.

### 구현 방향

MVP에서는 다음 방식을 조합한다.

| 방식 | 설명 | 우선순위 |
|---|---|---|
| MediaStore 감시 | Android 이미지 저장소에서 Screenshots 경로 신규 파일 감지 | 1순위 |
| ContentObserver | MediaStore 변경 이벤트 감지 | 1순위 |
| 주기적 백그라운드 스캔 | 누락된 캡처 보완 | 2순위 |
| Android 14 Screenshot Detection API | 앱 내부 화면 캡처 감지용 보조 기능 | 3순위 |

### 요구사항

| ID | 요구사항 |
|---|---|
| FR-001 | 앱은 새 스크린샷 파일 생성을 감지해야 한다. |
| FR-002 | 앱은 이미 처리한 스크린샷을 중복 처리하지 않아야 한다. |
| FR-003 | 사용자는 자동 감지를 ON/OFF 할 수 있어야 한다. |
| FR-004 | 앱은 최근 N일간의 기존 스크린샷을 수동으로 가져올 수 있어야 한다. |
| FR-005 | 앱은 감지 실패 시 주기적 백업 스캔으로 누락 파일을 찾을 수 있어야 한다. |

---

## 8.2 이미지 접근 및 권한

### 설명

앱은 스크린샷 이미지를 읽기 위해 Android 미디어 접근 권한을 요청해야 한다.

### 권한 정책

| Android 버전 | 접근 방식 |
|---|---|
| Android 13 이상 | `READ_MEDIA_IMAGES` |
| Android 10 이상 | MediaStore 기반 Scoped Storage |
| Android 9 이하 | Legacy external storage 고려 |

### 요구사항

| ID | 요구사항 |
|---|---|
| FR-010 | 앱은 필요한 이미지 접근 권한을 명확하게 설명해야 한다. |
| FR-011 | 권한 거부 시 제한 모드로 동작해야 한다. |
| FR-012 | 사용자가 권한을 나중에 다시 허용할 수 있어야 한다. |
| FR-013 | 앱은 전체 파일 접근 권한을 기본 요구하지 않아야 한다. |

---

## 8.3 OCR 처리

### 설명

앱은 스크린샷 이미지 안의 텍스트를 추출해야 한다.

### OCR 엔진 후보

| 옵션 | 장점 | 단점 |
|---|---|---|
| Google ML Kit Text Recognition | 안드로이드 친화적, 온디바이스 가능 | 복잡한 레이아웃 보존 한계 |
| Google Cloud Vision API | 높은 정확도 | 비용, 네트워크 필요 |
| OpenAI / Gemini Vision API | 요약, 구조화 강함 | 비용, 개인정보 이슈 |
| Tesseract | 오픈소스 | 모바일 정확도/성능 튜닝 필요 |

### MVP 권장

```txt
기본 OCR: Google ML Kit 온디바이스 OCR
고급 분석: 선택적으로 Vision LLM API
```

### 요구사항

| ID | 요구사항 |
|---|---|
| FR-020 | 앱은 이미지에서 텍스트를 추출해야 한다. |
| FR-021 | OCR 결과는 원문 순서를 최대한 보존해야 한다. |
| FR-022 | 한국어, 영어, 일본어 인식을 지원해야 한다. |
| FR-023 | OCR 실패 시 실패 상태와 원본 이미지를 보존해야 한다. |
| FR-024 | 사용자는 OCR 재시도를 실행할 수 있어야 한다. |

---

## 8.4 이미지 내용 분석

### 설명

OCR만으로는 이미지의 의미를 충분히 파악하기 어렵다. 앱은 이미지의 내용을 분석해 카테고리, 제목, 태그, 요약을 생성해야 한다.

### 분석 항목

| 항목 | 설명 |
|---|---|
| title | 캡처 내용 기반 제목 |
| category | 대분류 |
| subcategory | 소분류 |
| summary | 짧은 요약 |
| tags | 검색용 태그 |
| source_type | 웹페이지, SNS, 메신저, 코드, 문서, 쇼핑, 강의 등 |
| language | 주요 언어 |
| entities | 사람, 서비스명, 가격, 날짜, 링크 등 |
| sensitivity | 민감정보 가능성 |

### 요구사항

| ID | 요구사항 |
|---|---|
| FR-030 | 앱은 OCR 결과와 이미지 정보를 바탕으로 제목을 생성해야 한다. |
| FR-031 | 앱은 적절한 카테고리를 자동 분류해야 한다. |
| FR-032 | 앱은 태그를 자동 생성해야 한다. |
| FR-033 | 앱은 짧은 요약을 생성해야 한다. |
| FR-034 | 앱은 민감정보 가능성이 있는 이미지를 감지해야 한다. |
| FR-035 | 민감정보 감지 시 클라우드 업로드 전 사용자 확인 옵션을 제공해야 한다. |

---

## 8.5 Markdown 생성

### 설명

앱은 스크린샷 내용을 Markdown 파일로 변환해야 한다.

### Markdown 생성 원칙

1. 원문 보존 우선
2. 요약은 원문과 분리
3. 이미지 원본 경로 포함
4. OCR 신뢰도 표시
5. 자동 태그 포함
6. 사람이 읽기 쉬운 구조 유지
7. Obsidian 호환성 고려

### 기본 Markdown 템플릿

```md
# {{title}}

- Captured at: {{captured_at}}
- Processed at: {{processed_at}}
- Source app: {{source_app}}
- Category: {{category}}
- Subcategory: {{subcategory}}
- Language: {{language}}
- OCR confidence: {{ocr_confidence}}
- Original image: {{image_filename}}

---

## Original Text

{{ocr_text_preserved}}

---

## Layout-Preserved Notes

{{layout_preserved_text}}

---

## Summary

{{summary}}

---

## Key Points

{{key_points}}

---

## Entities

{{entities}}

---

## Tags

{{tags}}

---

## Processing Metadata

```json
{{metadata_json}}
```
```

### 요구사항

| ID | 요구사항 |
|---|---|
| FR-040 | 앱은 스크린샷마다 Markdown 파일을 생성해야 한다. |
| FR-041 | Markdown은 원문 OCR 영역과 AI 요약 영역을 분리해야 한다. |
| FR-042 | Markdown에는 원본 이미지 파일명이 포함되어야 한다. |
| FR-043 | Markdown에는 태그와 카테고리가 포함되어야 한다. |
| FR-044 | 사용자는 Markdown 템플릿을 수정할 수 있어야 한다. |
| FR-045 | Markdown 파일명은 사람이 읽을 수 있어야 한다. |

---

## 8.6 클라우드 드라이브 저장

### 설명

MVP에서는 Google Drive를 1차 지원한다.

### 저장 전략

MVP에서는 일반 Drive 폴더 방식을 사용한다.

```txt
Google Drive/
  CaptureBrain/
    Inbox/
    Learning/
    Work/
    Development/
    Business/
    Finance/
    Shopping/
    Personal/
    Archive/
```

### 요구사항

| ID | 요구사항 |
|---|---|
| FR-050 | 사용자는 Google 계정으로 로그인할 수 있어야 한다. |
| FR-051 | 앱은 Google Drive에 루트 폴더를 생성해야 한다. |
| FR-052 | 앱은 카테고리별 하위 폴더를 생성해야 한다. |
| FR-053 | 앱은 원본 이미지와 Markdown 파일을 함께 업로드해야 한다. |
| FR-054 | 업로드 실패 시 로컬 큐에 보관하고 재시도해야 한다. |
| FR-055 | 사용자는 저장 루트 폴더명을 변경할 수 있어야 한다. |
| FR-056 | 사용자는 Wi-Fi 연결 시에만 업로드하도록 설정할 수 있어야 한다. |

---

## 8.7 자동 디렉토리 생성

### 설명

앱은 분석 결과를 바탕으로 적절한 폴더를 자동 생성한다.

### 기본 분류 체계

```txt
CaptureBrain/
  00_Inbox/
  01_Learning/
    AI/
    Language/
    Programming/
    Business/
  02_Work/
    Meetings/
    Documents/
    References/
  03_Development/
    Errors/
    Code/
    Docs/
    Architecture/
  04_Business/
    Competitors/
    Pricing/
    Marketing/
    Ideas/
  05_Content/
    Writing/
    Design/
    Social/
  06_Shopping/
    Products/
    Prices/
  07_Personal/
  99_Archive/
```

### 폴더 생성 규칙

| 조건 | 저장 위치 |
|---|---|
| 코드/에러 메시지 | `03_Development/Errors` |
| API 문서/기술 문서 | `03_Development/Docs` |
| 강의/학습 자료 | `01_Learning/{topic}` |
| 가격표/경쟁 서비스 | `04_Business/Pricing` 또는 `Competitors` |
| SNS 글 | `05_Content/Social` |
| 제품 정보 | `06_Shopping/Products` |
| 분류 불명 | `00_Inbox` |

### 요구사항

| ID | 요구사항 |
|---|---|
| FR-060 | 앱은 분석 결과에 따라 저장 폴더를 자동 결정해야 한다. |
| FR-061 | 앱은 존재하지 않는 폴더를 자동 생성해야 한다. |
| FR-062 | 분류 신뢰도가 낮으면 Inbox에 저장해야 한다. |
| FR-063 | 사용자는 자동 분류 결과를 수정할 수 있어야 한다. |
| FR-064 | 수정된 분류는 향후 추천에 반영되어야 한다. |

---

## 8.8 처리 이력 및 상태 관리

### 설명

모든 스크린샷은 처리 상태를 가져야 한다.

### 상태값

| 상태 | 설명 |
|---|---|
| detected | 새 스크린샷 감지됨 |
| queued | 처리 대기 중 |
| processing | OCR/분석 중 |
| markdown_created | Markdown 생성 완료 |
| uploading | 클라우드 업로드 중 |
| completed | 완료 |
| failed_ocr | OCR 실패 |
| failed_upload | 업로드 실패 |
| skipped | 사용자가 제외 |
| sensitive_pending | 민감정보 확인 대기 |

### 요구사항

| ID | 요구사항 |
|---|---|
| FR-070 | 앱은 처리 상태를 로컬 DB에 저장해야 한다. |
| FR-071 | 사용자는 최근 처리 이력을 볼 수 있어야 한다. |
| FR-072 | 실패 항목을 재시도할 수 있어야 한다. |
| FR-073 | 특정 이미지를 제외 처리할 수 있어야 한다. |
| FR-074 | 중복 파일은 재처리하지 않아야 한다. |

---

# 9. 화면 설계

## 9.1 온보딩 화면

### 목적

사용자에게 앱의 역할과 필요한 권한을 설명한다.

### 구성

1. 앱 소개
2. 스크린샷 자동 감지 설명
3. 이미지 접근 권한 요청
4. Google Drive 연결
5. 저장 폴더 선택
6. 개인정보 처리 방식 안내
7. 자동 처리 시작

---

## 9.2 홈 화면

### 구성

| 영역 | 내용 |
|---|---|
| 상단 | 자동 감지 ON/OFF |
| 상태 카드 | 오늘 처리한 스크린샷 수 |
| 큐 상태 | 대기/성공/실패 |
| 최근 캡처 | 최근 10개 항목 |
| 빠른 액션 | 수동 스캔, 재시도, Drive 열기 |

---

## 9.3 캡처 상세 화면

### 구성

1. 원본 이미지 미리보기
2. 생성된 제목
3. 카테고리/태그
4. OCR 원문
5. 요약
6. Markdown 미리보기
7. 저장 위치
8. 재처리 버튼
9. 업로드 상태

---

## 9.4 설정 화면

### 구성

| 설정 | 설명 |
|---|---|
| 자동 감지 | ON/OFF |
| 기존 스크린샷 가져오기 | 최근 1일/7일/30일 |
| 저장 드라이브 | Google Drive |
| 루트 폴더명 | 기본값 CaptureBrain |
| 업로드 조건 | Wi-Fi only / 배터리 절약 |
| OCR 언어 | 한국어/영어/일본어 |
| AI 분석 사용 | ON/OFF |
| 민감정보 확인 | 자동 업로드 전 확인 |
| Markdown 템플릿 | 사용자 편집 |
| 로컬 데이터 삭제 | 캐시/이력 삭제 |

---

# 10. 비기능 요구사항

## 10.1 성능

| ID | 요구사항 |
|---|---|
| NFR-001 | 새 스크린샷 감지 후 10초 이내 처리 큐에 등록되어야 한다. |
| NFR-002 | 일반 스크린샷 1장은 30초 이내 OCR 처리 완료를 목표로 한다. |
| NFR-003 | 앱은 백그라운드에서 배터리를 과도하게 사용하지 않아야 한다. |
| NFR-004 | 대량 처리 시 WorkManager를 사용해 안정적으로 처리해야 한다. |

## 10.2 안정성

| ID | 요구사항 |
|---|---|
| NFR-010 | 네트워크가 끊겨도 로컬 큐에 보관되어야 한다. |
| NFR-011 | 앱 종료 후에도 처리 대기 항목이 유지되어야 한다. |
| NFR-012 | 동일 파일 중복 업로드를 방지해야 한다. |
| NFR-013 | 업로드 실패 시 지수 백오프 방식으로 재시도해야 한다. |

## 10.3 보안 및 개인정보

| ID | 요구사항 |
|---|---|
| NFR-020 | OAuth 토큰은 안전하게 저장해야 한다. |
| NFR-021 | 민감정보 가능성이 있는 이미지는 사용자 확인 옵션을 제공해야 한다. |
| NFR-022 | 사용자는 특정 앱/폴더의 스크린샷을 제외할 수 있어야 한다. |
| NFR-023 | OCR/AI 분석에 외부 API를 사용할 경우 명확히 고지해야 한다. |
| NFR-024 | 로컬 DB에는 필요한 최소 메타데이터만 저장해야 한다. |

---

# 11. 기술 설계 방향

## 11.1 권장 기술 스택

| 영역 | 기술 |
|---|---|
| Android | Kotlin |
| UI | Jetpack Compose |
| Background Task | WorkManager |
| Local DB | Room |
| Image Detection | MediaStore + ContentObserver |
| OCR | Google ML Kit Text Recognition |
| Cloud | Google Drive API v3 |
| Auth | Google Identity Services / OAuth |
| Markdown 생성 | Kotlin 템플릿 엔진 또는 자체 생성기 |
| AI 분석 | 선택형 Vision LLM API |
| Crash/Error | Firebase Crashlytics |
| Analytics | Firebase Analytics 또는 PostHog |

---

## 11.2 아키텍처 개요

```txt
[Android Screenshot]
        ↓
[MediaStore Observer]
        ↓
[Capture Queue]
        ↓
[Image Loader]
        ↓
[OCR Processor]
        ↓
[AI Classifier / Summarizer]
        ↓
[Markdown Generator]
        ↓
[Folder Resolver]
        ↓
[Google Drive Uploader]
        ↓
[Local History DB]
```

---

## 11.3 로컬 데이터 모델

### CaptureItem

```json
{
  "id": "uuid",
  "mediaStoreId": "string",
  "imageUri": "content://...",
  "imageHash": "sha256",
  "fileName": "Screenshot_20260526_143200.png",
  "capturedAt": "2026-05-26T14:32:00+09:00",
  "detectedAt": "2026-05-26T14:32:05+09:00",
  "status": "completed",
  "sourceApp": "unknown",
  "ocrText": "...",
  "title": "AI Agent Architecture",
  "summary": "...",
  "category": "Development",
  "subcategory": "AI",
  "tags": ["AI", "Agent", "Architecture"],
  "driveImageFileId": "string",
  "driveMarkdownFileId": "string",
  "driveFolderId": "string",
  "errorMessage": null
}
```

---

# 12. AI 처리 정책

## 12.1 원문 보존 우선

AI는 원문을 멋대로 고쳐 쓰면 안 된다.  
Markdown에는 반드시 다음 섹션을 분리한다.

1. OCR 원문
2. 레이아웃 보존 텍스트
3. AI 요약
4. 태그
5. 메타데이터

## 12.2 프롬프트 기본 정책

```txt
You are converting a screenshot into a Markdown knowledge note.

Rules:
1. Preserve original text as much as possible.
2. Do not invent missing text.
3. Separate extracted original text from summary.
4. Generate a short title.
5. Classify the screenshot into a folder path.
6. Generate useful tags.
7. If the image may contain sensitive information, mark sensitivity as true.
8. Output structured JSON.
```

## 12.3 AI 출력 스키마

```json
{
  "title": "string",
  "category": "string",
  "subcategory": "string",
  "folderPath": "string",
  "summary": "string",
  "keyPoints": ["string"],
  "tags": ["string"],
  "entities": {
    "people": [],
    "products": [],
    "urls": [],
    "dates": [],
    "prices": []
  },
  "sensitivity": {
    "isSensitive": false,
    "reason": null
  }
}
```

---

# 13. MVP 개발 우선순위

## Phase 1: 로컬 자동 수집

목표: 스크린샷을 감지하고 앱 안에서 처리 이력을 볼 수 있게 한다.

포함 기능:

1. MediaStore 스크린샷 감지
2. 권한 요청
3. 로컬 DB 저장
4. 최근 스크린샷 목록
5. 수동 스캔
6. 중복 방지

---

## Phase 2: OCR + Markdown 생성

목표: 이미지 내용을 Markdown으로 바꾼다.

포함 기능:

1. ML Kit OCR
2. OCR 원문 저장
3. Markdown 생성
4. Markdown 미리보기
5. 로컬 파일 저장
6. 재처리 기능

---

## Phase 3: Google Drive 업로드

목표: 세컨드브레인 폴더를 Drive에 만든다.

포함 기능:

1. Google 로그인
2. Drive 권한 요청
3. 루트 폴더 생성
4. 카테고리 폴더 생성
5. 이미지 업로드
6. Markdown 업로드
7. 실패 재시도

---

## Phase 4: AI 분류/요약

목표: 단순 OCR 앱에서 지식 정리 앱으로 발전시킨다.

포함 기능:

1. 제목 자동 생성
2. 카테고리 분류
3. 태그 생성
4. 요약 생성
5. 민감정보 감지
6. 폴더 추천

---

## Phase 5: 고도화

목표: 진짜 세컨드브레인으로 만든다.

포함 기능:

1. 앱 내 검색
2. 자연어 검색
3. 비슷한 캡처 묶기
4. 주간 리뷰
5. Obsidian 호환 폴더 구조
6. Notion/Dropbox/OneDrive 연동
7. 로컬 임베딩 검색
8. RAG 질의응답

---

# 14. 성공 지표

## 14.1 제품 지표

| 지표 | 목표 |
|---|---|
| 설치 후 권한 완료율 | 70% 이상 |
| 첫 스크린샷 처리 성공률 | 85% 이상 |
| OCR 성공률 | 90% 이상 |
| Drive 업로드 성공률 | 95% 이상 |
| 7일 유지율 | 30% 이상 |
| 주간 처리 스크린샷 수 | 사용자당 20개 이상 |
| 수동 재분류 비율 | 20% 이하 |
| 실패 재시도 성공률 | 80% 이상 |

## 14.2 핵심 North Star Metric

```txt
주간 자동 지식화 완료 스크린샷 수
```

즉, 단순히 캡처를 많이 하는 게 아니라, Markdown 노트로 성공적으로 변환되어 Drive에 저장된 스크린샷 수가 핵심 지표다.

---

# 15. 리스크 및 대응

| 리스크 | 설명 | 대응 |
|---|---|---|
| 백그라운드 감지 누락 | Android 제조사별 백그라운드 제한 | ContentObserver + 주기적 스캔 병행 |
| 권한 거부 | 이미지 접근 권한 거부 가능 | 온보딩에서 가치 설명 |
| OCR 정확도 낮음 | 복잡한 이미지/작은 글씨 | 원본 이미지 항상 보존 |
| Drive API 인증 복잡성 | OAuth 설정과 검수 필요 | 최소 권한 범위 사용 |
| 개인정보 우려 | 민감한 스크린샷 업로드 문제 | 로컬 처리 옵션, 업로드 전 확인 |
| 비용 증가 | AI Vision API 비용 | 기본 OCR 무료, 고급 분석 유료 |
| 폴더 분류 오류 | 잘못된 카테고리 | Inbox fallback, 사용자 수정 학습 |

---

# 16. 수익화 모델

## 16.1 무료 플랜

| 항목 | 제한 |
|---|---|
| 월 처리 수 | 100장 |
| OCR | 지원 |
| Google Drive 저장 | 지원 |
| AI 요약 | 월 30장 |
| 자동 분류 | 기본 분류 |
| Markdown 템플릿 | 기본 템플릿 |

## 16.2 Pro 플랜

예상 가격: 월 4,900원 ~ 9,900원

| 항목 | 제공 |
|---|---|
| 월 처리 수 | 2,000장 |
| AI 요약 | 무제한 또는 높은 한도 |
| 고급 분류 | 지원 |
| 민감정보 감지 | 지원 |
| Obsidian 호환 모드 | 지원 |
| 주간 리뷰 | 지원 |
| 자연어 검색 | 지원 |

## 16.3 Lifetime 플랜

예상 가격: 49,000원 ~ 99,000원

초기 개인 개발자 앱이라면 Lifetime 딜로 초기 유저를 모으는 전략도 가능하다.

---

# 17. 네이밍 후보

| 이름 | 느낌 |
|---|---|
| CaptureBrain | 직관적, 글로벌 |
| SnapMind | 가볍고 기억에 남음 |
| ScreenVault | 보관소 느낌 |
| SecondShot | 세컨드브레인 + 스크린샷 |
| MindShot | 캡처가 생각이 되는 느낌 |
| ClipMind | 클립 기반 지식화 |
| RecallShot | 나중에 다시 떠올리는 느낌 |
| ShotNote | 단순하고 명확 |
| CaptureNote | 실용적 |
| BrainFolder | 폴더 기반 세컨드브레인 느낌 |

MVP 이름은 **CaptureBrain** 또는 **ShotNote**를 우선 검토한다.  
CaptureBrain은 제품 비전이 크고, ShotNote는 기능이 바로 이해된다.

---

# 18. MVP 한 줄 스펙

```txt
안드로이드에서 새 스크린샷을 감지하고, OCR로 텍스트를 추출한 뒤, 제목/태그/요약이 포함된 Markdown 파일과 원본 이미지를 Google Drive의 자동 분류 폴더에 저장한다.
```

---

# 19. 최종 개발 권장 방향

첫 버전은 너무 똑똑하게 만들지 않는다.  
이 앱의 핵심은 AI가 아니라 자동 파이프라인이다.

MVP 우선순위는 다음과 같다.

```txt
1. 스크린샷 감지
2. 중복 방지
3. OCR
4. Markdown 생성
5. Drive 저장
6. 자동 폴더 분류
7. AI 요약
```

AI 요약보다 중요한 것은 “캡처했더니 실제로 Drive에 Markdown이 생긴다”는 첫 경험이다.  
이 첫 경험이 완성되면, 이후 검색, 요약, 리뷰, RAG 기능은 자연스럽게 확장할 수 있다.

---

# 20. Codex 구현용 다음 단계

## 20.1 먼저 만들어야 할 모듈

1. Android 프로젝트 생성
2. 권한 요청 모듈
3. MediaStore 스캔 모듈
4. ContentObserver 모듈
5. Room DB 모델
6. WorkManager 큐
7. OCR Processor
8. Markdown Generator
9. Google Drive Auth
10. Google Drive Uploader
11. Capture History UI
12. Settings UI

## 20.2 MVP 완료 기준

MVP는 다음 조건을 만족하면 완료로 본다.

1. 앱 설치 후 권한 설정 가능
2. 새 스크린샷 감지 가능
3. 기존 스크린샷 수동 스캔 가능
4. OCR 결과 생성 가능
5. Markdown 파일 생성 가능
6. Google Drive에 이미지와 Markdown 업로드 가능
7. 실패 항목 재시도 가능
8. 앱에서 처리 이력 확인 가능
