# SSOK BANK

LG CNS Am Inspire Camp 1기 3조 금융팀의 최종 프로젝트 **SSOK BANK** 백엔드(Spring Boot) 애플리케이션입니다.  
이 프로젝트는 SSOK APP의 뱅킹 기능을 제공하며, 계좌 조회, 생성, 입출금, 이체, 수신 상품 이자 지급 등 핵심 금융 기능을 지원합니다.

---

## 🏛️ 주요 기능

- **사용자 생성**: 사용자 생성
- **계좌 생성**: 신규 계좌 개설
- **상품 조회**: 수신 상품 목록 조회
- **계좌 조회**: 사용자별 계좌 목록 조회
- **잔액 확인**: 각 계좌의 현재 잔액 확인
- **입출금**: 지정 계좌로 입금, 출금 처리
- **이체 기능**: 사용자 간/자체 계좌 간 이체
- **거래 내역 조회**: 일자 및 계좌별 거래 내역 확인
- **거래 유효성 검사**: 예금주명 조회, 휴면 여부 확인, 잔액 및 송금 한도 검사
- **이자 지급**: 상품에 적용된 이자율에 맞춰 매일 이자 지급
---

## 🧱 기술 스택

| 구성 요소           | 설명                       |
|-----------------|--------------------------|
| Spring Boot     | RESTful API 서버 구현        |
| Spring Data JPA | 데이터 접근 계층 구성             |
| MariaDB         | 개발용 임베디드 DB 또는 운영용 RDBMS |
| Lombok          | 반복 코드 자동 생성              |
| Maven           | 빌드 및 의존성 관리              |
| Kafka           | 대외 송신 내역 관리              |

---

## ⚙️ 설치 및 실행 (개발 환경 기준)

1. **소스 코드 클론**
    ```bash
    git clone https://github.com/Team-SSOK/ssok-bank.git
    cd ssok-bank
    ```
2. **환경 설정**  
   `src/main/resources/application.yml` 또는 `application-dev.yml`에 DB 연결 및 포트, 기타 설정 구성.
3. **의존성 설치 및 컴파일**
    ```bash
    mvn clean install
    ```
4. **서버 실행**
    ```bash
    mvn spring-boot:run
    ```
   또는
    ```bash
    java -jar target/ssok-bank-0.0.1-SNAPSHOT.jar
    ```
5. **API 테스트**
    - 자세한 API 명세는 [bank_spec.md](./bank_spec.md)를 참조하세요.
    - [POST] `/api/bank/user` – 사용자 생성
    - [GET] `/api/bank/good` – 상품 조회
    - [POST] `/api/bank/account` – 계좌 개설
    - [POST] `/api/bank/transfer/withdraw` – 출금 이체
    - [POST] `/api/bank/transfer/deposit` – 입금 이체
    - [POST] `/api/bank/account/search` – 계좌 목록 조회
    - [POST] `/api/bank/account/history` - 계좌 거래 내역 조회
    - [POST] `/api/bank/account/balance` - 계좌 잔액 조회
    - [POST] `/api/bank/account/owner` - 예금주명 조회
    - [POST] `/api/bank/account/valid` - 계좌 유효성 검사
    - [POST] `/api/bank/account/dormant` - 휴면 계좌 여부 검사
    - [POST] `/api/bank/account/transferable` - 잔액 및 송금 한도 검사


---

## 🔧 개발 관련 설정

- **프로필 활성화**  
  `-Dspring.profiles.active=dev` 또는 `dev` 프로필 기본
- **DB 초기화**: `init.sql` 로딩
- **테스트 코드**: JUnit 기반 단위/통합 테스트

---

## 🧪 테스트 및 검증

- `mvn test` 또는 IDE 내 테스트 실행
- 통합 테스트: MockMvc 활용한 REST API 검증

---

## 🚀 배포

- Dockerfile 및 CI/CD 설정 필요 (DevOps/ssok-deploy 리포지토리 참고)
- 운영 DB(MySQL 등) 및 AWS/GCP 배포 환경 연동

---

## 👨‍👩‍👧‍👦 팀 정보

- **Team‑SSOK** – LG CNS Am Inspire Camp 1기 3조 금융팀
- **관련 레포지토리**
    - SSOK APP 프론트엔드: `ssok-frontend`
    - 배포 자동화: `ssok-deploy`
    - 오픈뱅킹 연동: `ssok-openbanking`

---




