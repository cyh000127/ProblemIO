# 📸 Problem.io — 이미지 기반 퀴즈 플랫폼 (WIP)

Problem.io는 **이미지 한 장으로 직관적·시각적인 퀴즈를 만들고 풀 수 있는 UGC 기반 플랫폼**입니다. 단순한 퀴즈 앱을 넘어 좋아요·팔로우·제작자 프로필 등 **SNS형 상호작용**을 제공해, 퀴즈 제작자와 플레이어가 자연스럽게 연결되는 것을 목표로 합니다.

## 주요 기능 흐름

### 1) 퀴즈 제작
- 제목/설명 입력, 썸네일 및 문제 이미지 업로드
- 보기 생성 및 정답 지정(동의어 등 복수 정답 처리 가능)
- 공개/비공개 설정 후 Spring Boot API를 통해 저장

### 2) 퀴즈 풀이
- 진행도 표시 + 문제 이미지 표시
- 보기 선택 시 문항별 즉시 피드백
- 마지막 문제 제출 시 최종 결과 출력
- 풀이 상태를 Pinia Store로 일관 관리

### 3) 제출 및 채점
- 문항별 정답 여부, 전체 점수, 선택 보기, 플레이 시간 저장
- 마이페이지에서 제출 기록을 다시 확인 가능

### 4) 소셜 기능 (SNS-like)
- 퀴즈 좋아요 / 제작자 팔로우
- 제작자 프로필 방문 및 제작 퀴즈 목록 조회
- 마이페이지에서 좋아요/팔로우/제출 기록 관리

### 🧩 별도의 데이터셋이 필요 없는 이유
- **UGC 기반**: 모든 문제/이미지/보기는 사용자가 직접 업로드
- 대규모 선행 데이터셋 없이도 서비스 확장
- 초기 데모 검증을 위해 일시적으로 크롤링 이미지로 예시 퀴즈를 제작했으나, 본질적 구조는 UGC

## 아키텍처 & 스택

- **Frontend**: Vue 3 + Vite, Pinia(상태), Vue Router, PrimeVue UI, Axios 인터셉터(JWT 자동 포함)
- **Backend**: Spring Boot 3.5, Spring Security + JWT 무상태 인증, MyBatis(XML Mapper), Controller → Service → Mapper 계층, 공통 `ApiResponse`
- **Database**: MySQL, 정규화된 퀴즈/문항/보기/제출/팔로우/좋아요 테이블 모델
- **Infra/기타**: 파일 업로드 지원, Google SMTP 기반 이메일 인증, 로깅 레벨/마이바티스 설정

## 프로젝트 구조

```
ProblemIO/
├─ frontend/       # Vue 3 + Vite 클라이언트
│  ├─ src/api      # Axios 인스턴스 및 도메인별 API
│  ├─ src/views    # 페이지 컴포넌트(Home, Feed, Quiz, Profile, Create, Auth 등)
│  └─ public       # 정적 자원(아이콘, 샘플 이미지)
├─ backend/        # Spring Boot + MyBatis 서버
│  ├─ src/main/java/com/problemio
│  │  ├─ global   # 보안/예외/JWT/파일 업로드 공통 모듈
│  │  ├─ quiz     # 퀴즈 CRUD, 좋아요
│  │  ├─ question # 문항/보기 생성 및 업데이트
│  │  ├─ submission # 제출/채점/기록 조회
│  │  ├─ user     # 회원가입/로그인/프로필/팔로우
│  │  └─ comment  # 댓글 및 댓글 좋아요
│  └─ resources/mapper # MyBatis XML Mapper
└─ README.md       # (본 파일)
```

## 로컬 실행 가이드

### 사전 준비
- JDK 17, Node.js 20+
- MySQL (DB명 예: `problemIO`)
- 루트 `backend/.env`와 `frontend/.env`를 환경에 맞게 준비

### 1. Backend
```bash
cd backend
# .env 생성 (예시)
cat > .env <<'EOF'
DB_URL=jdbc:mysql://localhost:3306/problemIO?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
MAIL_USERNAME=your_gmail_account
MAIL_PASSWORD=your_gmail_app_password   # 구글 앱 비밀번호 사용
JWT_SECRET=change-me-to-a-long-secret
EOF

./gradlew bootRun  # 또는 ./gradlew build 후 java -jar build/libs/*.jar
```

### 2. Frontend
```bash
cd frontend
npm install
echo "VITE_API_BASE_URL=http://localhost:8080/api/" > .env
npm run dev
```

### 3. 실행 순서
1) MySQL 가동 및 DB 생성 → 2) 백엔드(`bootRun`) → 3) 프론트엔드(`npm run dev`)

## 데모 사용자 흐름
1. 회원가입 후 로그인  
2. 다른 사람이 만든 퀴즈 목록 조회(인기/최신)  
3. 흥미로운 퀴즈 선택 후 이미지 보며 풀이  
4. 각 문제별 즉시 피드백 확인  
5. 최종 점수 확인 및 좋아요  
6. 제작자 프로필 방문 → 다른 퀴즈 플레이  
7. 팔로우하여 이후 피드에 노출  
8. 마이페이지에서 제출 기록/좋아요/팔로우 내역 확인  

## 실행 화면 (예시)
- 이미지 추가 예정🤟

## 프로젝트 의의
- JWT + Spring Security 기반 실서비스형 인증/보안 학습
- 여러 관계형 테이블을 활용한 도메인 모델링 및 MyBatis SQL 제어 경험
- 이미지 업로드와 상태 관리(Pinia)까지 포함한 풀스택 E2E 구현 경험
