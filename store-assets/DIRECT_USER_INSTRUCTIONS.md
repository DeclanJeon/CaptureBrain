# 폰스가 직접 해야 하는 Play Store 출시 작업 지침서

이 파일은 내가 대신 할 수 없는 계정/심사/동의 작업만 모은 지침서입니다.

## 1. Google Play Console 계정 작업

1. Google Play Console에 로그인한다.
2. 앱 만들기를 누른다.
3. 앱 이름: `캡처브레인`
4. 기본 언어: 한국어
5. 앱/게임: 앱
6. 무료/유료: 처음에는 무료 권장
7. 개발자 프로그램 정책 동의 후 생성한다.

## 2. Google Cloud / OAuth 작업

Google Drive 로그인이 있는 앱이라 이 단계는 직접 확인해야 합니다.

1. Google Cloud Console에 접속한다.
2. CaptureBrain 프로젝트를 선택한다.
3. OAuth consent screen을 Production 상태로 준비한다.
4. 앱 이름/지원 이메일/개발자 연락처 이메일을 실제 값으로 입력한다.
5. 권한 범위는 가능하면 `drive.file`만 유지한다.
6. Android OAuth Client에 다음 값을 등록한다.
   - package name: `com.ponslink.capturebrain`
   - SHA-1: 릴리즈 키스토어에서 나온 SHA-1
7. 릴리즈 SHA-1/SHA-256은 디버그 키와 다르므로 반드시 새로 등록한다.

릴리즈 SHA 확인 명령과 업로드용 AAB 빌드는 아래 파일에 따로 정리해 두었습니다.

`store-assets/play-console/release-build.md`

## 3. 개인정보처리방침 URL 준비

Play Console에는 공개 URL이 필요합니다.

선택지:

- ponslink.com 또는 bom.ponslink.com 같은 본인 도메인에 HTML로 게시
- GitHub Pages로 게시
- Notion 공개 페이지는 가능하긴 하지만 장기적으로는 도메인 페이지 권장

게시할 초안 파일:

`store-assets/play-console/privacy-policy-ko.md`

직접 수정해야 할 항목:

- 시행일
- 개발자명/사업자명
- 문의 이메일
- 실제 개인정보처리방침 URL
- 외부 AI API 사용 여부

## 4. Play Console 등록정보 입력

다음 파일 내용을 복사해서 입력한다.

- `store-assets/play-console/listing-ko.md`

업로드할 이미지:

- 앱 아이콘: `store-assets/icons/capturebrain-icon-512.png`
- 기능 그래픽: `store-assets/feature-graphic/capturebrain-feature-graphic-1024x500.png`
- 스마트폰 스크린샷:
  - `store-assets/screenshots/01-setup-first-home.png`
  - `store-assets/screenshots/02-auto-category.png`
  - `store-assets/screenshots/03-drive-structure.png`
  - `store-assets/screenshots/04-simple-flow.png`

## 5. 앱 콘텐츠 설문

Play Console의 앱 콘텐츠 섹션에서 직접 입력한다.

- 개인정보처리방침: 공개 URL 입력
- 광고 포함 여부: 광고 SDK가 없으면 아니오
- 앱 액세스 권한: 로그인 없이 기본 화면 접근 가능. Google Drive 연결은 사용자가 선택
- 콘텐츠 등급: 생산성/도구 앱 기준으로 설문 진행
- 타겟층: 일반 사용자. 아동 대상 아님으로 진행 권장
- 뉴스 앱 여부: 아니오
- 정부/건강/금융 앱 여부: 해당 없으면 아니오
- 데이터 보안: `store-assets/play-console/data-safety-ko.md` 참고

## 6. 테스트 트랙

1. 내부 테스트 트랙을 만든다.
2. release AAB를 업로드한다.
3. 본인 Google 계정을 테스터로 추가한다.
4. Play Store 테스트 링크로 설치한다.
5. 실제 설치본에서 확인한다.
   - 앱 실행
   - Google Drive 연결
   - 저장 폴더명 변경
   - 스크린샷 권한 허용
   - 스크린샷 촬영
   - Drive에 이미지와 Markdown 생성 여부

## 7. 프로덕션 제출 전 직접 확인 체크리스트

- [ ] Play Console 개발자 계정 생성 완료
- [ ] 개인정보처리방침 URL 공개 완료
- [ ] 지원 이메일 입력 완료
- [ ] 릴리즈 SHA-1 Google Cloud에 등록 완료
- [ ] Google OAuth consent screen Production 상태 확인
- [ ] 내부 테스트 설치본에서 Drive 로그인 성공
- [ ] 실제 스크린샷 → Drive 업로드 성공
- [ ] 데이터 보안 답변이 실제 앱 동작과 일치
- [ ] 앱 설명에 과장된 표현 없음

## 주의

외부 AI API로 스크린샷 이미지나 텍스트를 보내는 기능을 추가하면, 개인정보처리방침과 데이터 보안 답변을 반드시 바꿔야 합니다.
