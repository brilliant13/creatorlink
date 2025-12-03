# creatorlink-backend

“유튜버·인플루언서 광고 링크의 클릭/전환 데이터를 수집·분석하는 B2B 백엔드 서비스”

---

# CreatorLink – 광고주용 크리에이터 링크 트래킹 SaaS

> 유튜버·인플루언서 캠페인 성과를 **링크 단위로 추적**하고,
> 광고주가 한 화면에서 **크리에이터별 / 캠페인별 성과를 관리**할 수 있는 B2B 백오피스 서비스입니다.

---

## 1. 프로젝트 개요

### 1-1. 한 줄 요약

크리에이터(유튜버, 인플루언서)에게 나가는 **개별 트래킹 링크**를 생성하고,
클릭 로그를 수집해 **광고주가 크리에이터별 성과를 비교·분석**할 수 있는 백엔드 중심 SaaS입니다.

### 1-2. 문제 상황

* 광고주는 여러 크리에이터에게 동시에 캠페인을 집행하지만,

  * **어떤 크리에이터가 얼마나 트래픽을 보냈는지**,
  * **어느 캠페인/링크가 효율이 좋은지** 한눈에 보기 어렵습니다.
* 대부분 Excel이나 수작업으로 관리하며,

  * 링크 관리가 복잡해지고
  * 크리에이터 성과 비교가 번거롭습니다.

### 1-3. 해결 방법

CreatorLink는 다음과 같은 흐름으로 문제를 해결합니다:

1. 광고주(User)가 캠페인(Campaign)을 등록
2. 각 캠페인에 참여하는 크리에이터(Creator)를 등록
3. 크리에이터별로 고유한 트래킹 링크(TrackingLink)를 생성 → `https://creator.link/t/{slug}`
4. 최종 랜딩 페이지로 리다이렉트하면서, 클릭 로그(ClickLog)를 적재
5. 광고주는 대시보드에서

   * 크리에이터별 누적 클릭 수
   * 캠페인별/기간별 클릭 추이를 조회
   * 상위 성과 크리에이터를 비교

> **핵심 포인트**:
> “누가 얼마나 실적을 냈는지”를 **링크 클릭 로그 기반으로 정량화**하는 것.

---

## 2. 주요 기능 (Features)

* **광고주(Advertiser) 관리**

  * 회원 가입/로그인 (추후 OAuth 확장 가능)
  * 본인이 소유한 캠페인/크리에이터만 조회

* **캠페인(Campaign) 관리**

  * 캠페인 생성/수정/삭제
  * 기간, 예산, 목표 등 메타데이터 관리

* **크리에이터(Creator) 관리**

  * 광고주별 크리에이터 등록
  * 크리에이터별 참여 캠페인 관리 (확장 예정)

* **트래킹 링크(TrackingLink) 생성**

  * 크리에이터·캠페인 단위로 slug 기반 고유 URL 생성
  * 예: `https://creator.link/t/ab12cd`

* **클릭 로그 수집 (ClickLog)**

  * `/t/{slug}` 접속 시:

    * 대상 링크로 302 Redirect
    * 동시에 DB에 클릭 로그 적재
  * IP, User-Agent, Referer, Timestamp 등 기록 (필드 구성은 실제 구현 기준)

* **통계/리포트**

  * 크리에이터별 누적 클릭 수
  * 캠페인별 클릭 수, 기간별 추이
  * 상위 성과 크리에이터 랭킹

---

## 3. 아키텍처 (Architecture)

### 3-1. 전체 구조

```text
[Client(React)]  --- HTTP/JSON --->  [Spring Boot API Server]  --- JPA ---> [MySQL]

                               └--- (optional) Redis cache for hot stats
```

* **Frontend**: React (광고주용 대시보드 UI)
* **Backend**: Spring Boot (REST API 서버)
* **DB**: MySQL (RDS로 확장 가능)
* **Infra**: AWS EC2 상 배포 예정 (또는 Docker-compose로 로컬 실행)

### 3-2. 주요 기술 스택

* **Backend**

  * Java 17+
  * Spring Boot
  * Spring Data JPA
  * (선택) QueryDSL
  * Lombok, Validation, Spring MVC

* **Database**

  * MySQL
  * JPA 기반 엔티티 매핑 + 인덱스 설계

* **Infra**

  * AWS EC2
  * (선택) Redis for caching
  * (선택) Docker

* **Etc**

  * Gradle 또는 Maven
  * Swagger / Springdoc OpenAPI (API 문서화)

---

## 4. 도메인 모델 & ERD

### 4-1. 주요 도메인 설명

* **User (Advertiser)**

  * 이 서비스를 사용하는 광고주 계정
  * 여러 Campaign과 여러 Creator를 소유

* **Campaign**

  * 광고주가 진행하는 하나의 마케팅 캠페인 단위
  * 예: “23년 11월 블랙프라이데이 세일”

* **Creator**

  * 유튜버, 인스타그램 인플루언서 등
  * 한 광고주(User)에 속한다 (현재 버전 기준 N:1 관계)

* **TrackingLink**

  * Creator + Campaign 조합으로 생성되는 고유 링크
  * URL slug를 통해 `/t/{slug}`로 접근

* **ClickLog**

  * TrackingLink가 클릭될 때마다 적재되는 이벤트 로그
  * 추후 통계/분석의 기반 데이터

### 4-2. ERD (예시)

```text
User (Advertiser)
  ├─ Campaign (1:N)
  └─ Creator (1:N)
  
Campaign
  └─ TrackingLink (1:N)

Creator
  └─ TrackingLink (1:N)

TrackingLink
  └─ ClickLog (1:N)
```

> 실제 ERD 다이어그램은 `docs/erd.png` 등으로 저장 후
> `![ERD](docs/erd.png)` 형태로 README에 추가할 수 있습니다.

---

## 5. API 설계

### 5-1. URL 설계 개요

* `POST   /api/users`               – 광고주 회원 가입 (optional)

* `POST   /api/auth/signup`         – 회원가입

* `POST   /api/auth/login`          – 로그인

* `POST   /api/campaigns`           – 캠페인 생성

* `GET    /api/campaigns`           – 광고주별 캠페인 목록 조회

* `GET    /api/campaigns/{id}`      – 캠페인 단건 조회

* `PUT    /api/campaigns/{id}`      – 캠페인 수정

* `DELETE /api/campaigns/{id}`      – 캠페인 삭제 (soft delete)

* `POST   /api/creators`            – 크리에이터 생성

* `GET    /api/creators`            – 광고주별 크리에이터 목록 조회

* `GET    /api/creators/{id}`       – 크리에이터 단건 조회

* `PUT    /api/creators/{id}`       – 크리에이터 수정

* `DELETE /api/creators/{id}`       – 크리에이터 삭제 (soft delete)

* `POST   /api/tracking-links`      – 트래킹 링크 생성

* `GET    /api/tracking-links`      – 캠페인 or 크리에이터별 링크 조회

* `DELETE /api/tracking-links/{id}` – 트래킹 링크 삭제 (soft delete)

* `GET    /t/{slug}`                – 트래킹 링크 라우팅 + 클릭 로그 기록

* `GET    /api/stats/campaigns`     – 캠페인별 통계

* `GET    /api/stats/creators`      – 크리에이터별 통계

### 5-2. 예시 API – 트래킹 링크 생성

요청:

```http
POST /api/tracking-links
Content-Type: application/json

{
  "campaignId": 1,
  "creatorId": 2,
  "targetUrl": "https://example.com/landing"
}
```

응답 (201 Created):

```json
{
  "id": 10,
  "slug": "ab12cd",
  "fullUrl": "https://creator.link/t/ab12cd",
  "campaignId": 1,
  "creatorId": 2
}
```

### 5-3. 예시 API – 트래킹 링크 클릭

요청:

```http
GET /t/ab12cd
```

동작:

1. `slug`를 기준으로 TrackingLink 조회
2. ClickLog 테이블에 로그 적재
3. `targetUrl`로 302 Redirect

---

## 6. 실행 방법 (Getting Started)

### 6-1. 사전 요구사항

* JDK 17+
* MySQL (또는 Docker로 MySQL 실행)
* (선택) Node.js & npm (프론트엔드 실행 시)

### 6-2. 환경 변수 설정 예시 (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/creatorlink?useSSL=false&serverTimezone=Asia/Seoul
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        show_sql: true
        format_sql: true
```

(실제 환경에 맞게 계정/비밀번호/옵션을 수정하세요.)

### 6-3. 백엔드 실행

Gradle 사용 시:

```bash
./gradlew bootRun
```

Maven 사용 시:

```bash
./mvnw spring-boot:run
```

### 6-4. API 문서 확인

* Swagger UI (예시):
  `http://localhost:8080/swagger-ui/index.html`
  (실제 설정에 맞게 경로 수정)

---

## 7. 폴더 구조 (예시)

```text
backend/
  src/
    main/
      java/com/jung/creatorlink/
        domain/
          user/
          campaign/
          creator/
          tracking/
            TrackingLink.java
            ClickLog.java
        dto/
          user/
          campaign/
          creator/
          tracking/
          stats/
        repository/
        service/
        controller/
      resources/
        application.yml
  build.gradle

frontend/
  src/
  package.json

docs/
  erd.png
  architecture.png
```

---

## 8. 트러블슈팅 & 기술적 의사결정

### 8-1. 왜 User–Creator를 N:1로 했는가?

* 초기 버전에서는 **광고주-크리에이터 관계를 단순화**하기 위해
  한 크리에이터는 한 광고주만 가진다는 가정으로 시작했습니다.
* 실제 비즈니스에서는 다대다 관계가 가능하지만,

  * MVP 단계에서는 광고주 내부에서 관리하는 “내 파트너 크리에이터 목록”에 집중
  * 추후 Creator–User를 M:N 관계로 확장할 수 있도록
    `CreatorAdvertiser` 조인 테이블 도입을 고려 중입니다.

### 8-2. TrackingLink에 slug를 사용한 이유

* 외부에 노출되는 링크가 단순 `id` 기반이면 예측 가능성이 높고,
* 해시/slug 기반 랜덤 문자열을 쓰면:

  * 보안성 향상
  * 외형적으로도 “트래킹 코드” 역할을 명확히 표현 가능

### 8-3. 통계를 어떻게 처리할지에 대한 고민

* ClickLog가 많아질수록 조회 성능 이슈가 발생할 수 있음

  * 인덱스 설계 (예: `tracking_link_id`, `created_at`)
  * 일별 집계 테이블 도입 (예: `daily_click_stats`)
  * Redis 캐시 활용으로 “실시간 조회” 성능 최적화 여지 확보

### 8-4. Soft Delete & Data Retention

CreatorLink에서는 **중요 도메인 엔티티에 soft delete 전략**을 적용했습니다.

* 대상 엔티티:

  * `Campaign`
  * `Creator`
  * `TrackingLink`
* 각 엔티티는 `Status` 필드를 갖습니다:

  * `ACTIVE` : 서비스에서 사용 중인 데이터
  * `INACTIVE` : 삭제 처리된(숨겨진) 데이터

#### 삭제 정책

* API 레벨에서의 “삭제” 요청은 실제 DB `DELETE`가 아니라,
  `status = INACTIVE` 로 변경하는 방식으로 처리합니다.
* 조회 시에는 기본적으로 `status = ACTIVE` 인 데이터만 노출합니다.

  * 예: 캠페인 목록, 크리에이터 목록, 트래킹 링크 목록 등

#### Soft delete를 선택한 이유

1. **실수/버그에 대한 방어**

   * 잘못된 요청으로 삭제해도, 데이터가 물리적으로 사라지지 않기 때문에
     `status`를 다시 `ACTIVE`로 돌려 복구할 수 있습니다.
2. **참조 무결성 & 이력 보존**

   * `TrackingLink`, `ClickLog` 등 다른 테이블이 참조하는 엔티티를 바로 지우면
     FK 제약 조건이나 통계/로그 데이터가 꼬일 수 있습니다.
   * soft delete를 사용하면 **Campaign/Creator/TrackingLink 이력**을 남기면서도
     UI/리다이렉트에서는 “삭제된 것처럼” 숨길 수 있습니다.
3. **광고 도메인의 특성**

   * “지금은 계약이 끝난 크리에이터/캠페인”이라도
     과거 성과는 여전히 의미가 있습니다.
   * soft delete를 통해 과거 캠페인/크리에이터 성과를 분석할 수 있는 여지를 남깁니다.

#### Data Retention (향후 계획)

* 장기적으로는, **오래된 INACTIVE 데이터**에 대해:

  * 예: `INACTIVE && updatedAt < 6개월 전`
  * 배치 작업(예: `@Scheduled`)으로 물리 삭제하는 전략을 고려하고 있습니다.
* 이를 통해:

  * 단기적으로는 복구 가능성을 유지하고
  * 장기적으로는 DB 스토리지 사용량을 관리하는 **균형 잡힌 구조**를 목표로 합니다.

> 이 구조를 통해
> “중요 도메인 데이터는 바로 지우지 않고,
> 비즈니스/운영 관점에서 안전하게 관리한다”는 설계를 유지하고 있습니다.

---

## 9. 향후 계획 (Roadmap)

* [ ] OAuth 로그인 (Google / Kakao 등) 도입
* [ ] M:N 관계를 지원하는 Creator–Advertiser 매핑 구조
* [ ] 일별/주별 통계 집계 배치 작업
* [ ] Redis 캐시 도입으로 실시간 대시보드 성능 개선
* [ ] Admin 페이지에서 간단한 그래프 시각화 (React + Chart 라이브러리)
* [ ] Soft delete 데이터에 대한 retention 배치(오래된 INACTIVE 물리 삭제)
* [ ] 다국어(영문) 지원 및 글로벌 캠페인 관리 기능

---

## 10. 작성자

* **이름**: 정웅 (Jung Woong)
* **Role**: 개인 사이드 프로젝트 – 설계부터 백엔드 구현 및 인프라까지 전담
* **Contact**: (이메일), (GitHub 프로필 링크)
