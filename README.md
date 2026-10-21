# SPRING_BOOT_20220672

자바웹프로그래밍(2) 수업 실습 프로젝트입니다.

- 학번: 20220672
- GitHub 저장소: [seungjoon1105/demo](https://github.com/seungjoon1105/demo)
- Java 25 · Spring Boot 4.1.1 · MySQL 8 · Thymeleaf · Spring Security

## 진행 현황

- 1주차 - 스프링 부트 개발환경 설정 완료
- 2주차 - `/hello` URL 매핑과 Thymeleaf 출력
- 3주차 - 포트폴리오 메인 화면(TemplateMo) 적용
- 4주차 - 데이터베이스 수정 완료 (MySQL + JPA 연동, `/testdb` 사용자 목록 출력)
- 5주차 - 로그인/로그아웃, 암호화 완료 + 연습문제(로그인 상태 유지, 비밀번호 확인)
- 6주차 - 권한(ROLE) 접근 제어, 관리자 페이지 분리 완료 + 연습문제(관리자 본인 보호, MANAGER 권한)

- 7주차 - 회원 관리 REST API(JSON) 완료 + 연습문제(회원 검색, 공통 오류 처리)

## 2주차 - 개발환경 설정 및 테스트

- JDK 25와 Spring Boot 4.1.1 개발환경 설정
- `DemoApplication` 실행 및 8080 포트 확인
- `/hello` URL 매핑과 Model 데이터의 Thymeleaf 출력
- [DemoApplication.java](src/main/java/com/example/demo/DemoApplication.java)
- [DemoController.java](src/main/java/com/example/demo/controller/DemoController.java)
- [hello.html](src/main/resources/templates/hello.html)

## 3주차 - 포트폴리오 프론트

- TemplateMo First Portfolio 기반 개인 포트폴리오 적용
- CSS, JavaScript, 이미지, 폰트를 `static` 폴더에 배치
- Thymeleaf 자원 경로와 한글 네비게이션 적용
- 소개, 기술, 프로젝트, 연락처 영역 구성
- 웹, AI, 보안, 게임 상세 페이지 연결
- [포트폴리오 index.html](src/main/resources/templates/index.html)
- 상세 페이지: [웹](src/main/resources/public/detailed_web.html) / [AI](src/main/resources/static/detailed_ai.html) / [보안](src/main/resources/static/detailed_security.html) / [게임](src/main/resources/static/detailed_game.html)

## 4주차 - 데이터베이스 연동 및 테스트

4주차 데이터베이스 수정 완료

- `pom.xml`에 Spring Data JPA, MySQL 드라이버(`mysql-connector-j`) 적용
- `application.properties`에서 MySQL `localhost:3306/spring` 연결
- DB 계정은 `application-secret.properties`로 분리하고 Git에서 제외
- 계층별 패키지 구성: `controller` / `model.domain` / `model.repository` / `model.service`
- 엔티티 [TestDB.java](src/main/java/com/example/demo/model/domain/TestDB.java) → 리포지토리 [TestRepository.java](src/main/java/com/example/demo/model/repository/TestRepository.java) → 서비스 [TestService.java](src/main/java/com/example/demo/model/service/TestService.java) → 컨트롤러 `/testdb`
- `findAll()`로 전체 데이터를 조회하고 Thymeleaf `th:each`로 목록 출력
- 연습문제: 나이(`age`), 성별(`gender`) 컬럼 추가, [testdb.html](src/main/resources/templates/testdb.html)에서 아이디·이름·나이·성별 출력
- SQL은 MySQL 확장에서 직접 실행하며 별도 SQL 폴더를 만들지 않음
- 자동 테스트는 H2 메모리 DB를 사용하며 실제 실행 DB와 분리

### 로컬 DB 준비

현재 작업 PC는 MySQL 연결과 실습 계정 준비를 완료했습니다. 다른 PC에서 처음 실행할 때는 아래 순서로 준비합니다.

1. MySQL 8을 실행합니다. macOS 실습 환경에서는 Docker를 사용합니다.
2. MySQL에 접속하여 수업용 데이터베이스를 생성합니다.

   ```sql
   show databases;
   create database spring;
   flush privileges;
   ```

3. [설정 예시](src/main/resources/application-secret.properties.example)를 참고하여 `src/main/resources/application-secret.properties`를 작성합니다.

   ```properties
   spring.datasource.username=root
   spring.datasource.password=본인의_MySQL_비밀번호
   app.security.remember-me-key=임의의_긴_비밀키
   ```

4. 서버를 실행하면 JPA가 `testdb`, `member` 테이블을 생성·갱신합니다.
5. MySQL 확장에서 `testdb` 데이터를 입력하고 `/testdb`에서 확인합니다. 최종 6주차 코드에서는 ADMIN 또는 MANAGER 권한이 필요합니다.

### 4주차 SQL 실습

PDF 27쪽의 최초 입력 SQL입니다. 기존 `id=1` 데이터가 있으면 중복 실행하지 않습니다.

```sql
insert into testdb (id, name)
values (1, '홍길동');
```

32쪽 연습문제는 나이·성별을 추가하고 MySQL 확장에서 직접 입력하는 과제입니다. 해당 페이지는 INSERT 문 전문 대신 결과 표를 제공합니다.

| 아이디 | 이름 | 나이 | 성별 |
| --- | --- | --- | --- |
| 1 | 홍길동 | 25 | 남 |
| 2 | 아저씨 | 40 | 남 |
| 3 | 김영희 | 30 | 여 |
| 4 | 이철수 | 22 | 남 |

## 5주차 - 로그인/로그아웃 및 암호화

5주차 로그인/로그아웃, 암호화 완료

- `pom.xml`에 Spring Security, `thymeleaf-extras-springsecurity6` 의존성 추가
- 교수님 제공 [login.html](src/main/resources/templates/login.html), [signup.html](src/main/resources/templates/signup.html) 적용
- 개인 포트폴리오의 `index.html`에 제공 자료의 네비게이션 교체부분 반영
- 로그인 상태에 따라 로그인·회원가입 또는 아이디·로그아웃 표시
- DB 비밀번호와 remember-me 비밀키를 `application-secret.properties`로 분리하고 `.gitignore`에 등록

| 파일 | 역할 |
| --- | --- |
| [SecurityConfig.java](src/main/java/com/example/demo/config/SecurityConfig.java) | BCrypt 암호화 빈, URL 접근 규칙, 폼 로그인, 로그아웃 |
| [MemberController.java](src/main/java/com/example/demo/controller/MemberController.java) | `/login`, `/signup` 화면과 회원가입 처리 |
| [Member.java](src/main/java/com/example/demo/model/domain/Member.java) | `member` 엔티티, 아이디 중복 금지, 권한 저장 |
| [MemberForm.java](src/main/java/com/example/demo/model/dto/MemberForm.java) | 회원가입 입력값 전달, 비밀번호 확인 |
| [MemberRepository.java](src/main/java/com/example/demo/model/repository/MemberRepository.java) | `findByUsername`, `existsByUsername` |
| [MemberService.java](src/main/java/com/example/demo/model/service/MemberService.java) | 가입 시 BCrypt 암호화, USER 지정, `UserDetailsService` 구현 |

- 가입 시 평문 비밀번호를 BCrypt 해시로 저장하고 Spring Security가 로그인 시 비교
- 로그인 성공 후 원래 요청 페이지로 복귀, 실패 시 오류 안내
- 로그아웃은 CSRF 토큰을 포함한 POST 폼으로 처리
- 로그아웃 시 세션 무효화 및 `JSESSIONID`, `remember-me` 쿠키 삭제

### 5주차 연습문제 - 로그인 기능 개선하기

- 로그인 상태 유지: 체크 후 로그인하면 유효기간 7일의 `remember-me` 쿠키 발급
- 세션이 없어져도 유효한 remember-me 쿠키로 인증 복원
- 비밀번호 확인: `password`와 `passwordConfirm`이 다르면 “비밀번호가 일치하지 않습니다.” 표시, DB 저장 차단
- 아이디 중복 가입 차단, 가입 화면의 아이디·이름 유지

## 6주차 - 권한 관리 및 관리자 페이지

6주차 권한(ROLE) 접근 제어, 관리자 페이지 분리 완료

- 교수님 제공 [mypage.html](src/main/resources/templates/mypage.html), [admin/members.html](src/main/resources/templates/admin/members.html), [error/403.html](src/main/resources/templates/error/403.html) 적용
- 네비게이션에 내 정보, 관리자 메뉴, USER·MANAGER·ADMIN 권한 배지 반영
- [AdminController.java](src/main/java/com/example/demo/controller/AdminController.java): 회원 목록, 권한 변경, 회원 삭제
- [SecurityConfig.java](src/main/java/com/example/demo/config/SecurityConfig.java): `/admin/**` ADMIN 제한, 메서드 보안 활성화
- [MemberController.java](src/main/java/com/example/demo/controller/MemberController.java): `Principal`의 로그인 아이디로 본인 정보 조회
- [MemberService.java](src/main/java/com/example/demo/model/service/MemberService.java): 회원 ID 순 조회, 허용된 권한 검사, 권한 변경·삭제
- 접근 제어: 화면 `sec:authorize` → URL `requestMatchers` → 서비스 `@PreAuthorize`
- 권한 변경·삭제 후 리다이렉트와 일회성 메시지로 결과 표시

### 관리자 계정 준비

다른 DB에서 처음 준비할 때는 `/signup`에서 `admin / 1234 / 관리자`로 가입한 뒤, 교수님 제공 SQL을 실행합니다.

```sql
SELECT id, username, name, role FROM member;
UPDATE member SET role = 'ADMIN' WHERE username = 'admin';
SELECT id, username, name, role FROM member;
```

권한 변경 후에는 로그아웃하고 다시 로그인해야 새 권한이 세션에 반영됩니다. 비밀번호는 회원가입 기능으로 BCrypt 저장하며, SQL로 평문 비밀번호를 직접 넣지 않습니다.

### 6주차 연습문제 - 관리자 보호 및 MANAGER 추가

- 관리자가 자신의 권한을 변경하거나 자신을 삭제하려 하면 거부
- 허용 권한에 MANAGER 추가
- `/testdb`는 ADMIN·MANAGER만 접근 허용
- USER에게 회원목록 메뉴 숨김

| 사용자 상태 | `/mypage` | `/testdb` | `/admin/**` |
| --- | --- | --- | --- |
| 비로그인 | 로그인 화면으로 이동 | 로그인 화면으로 이동 | 로그인 화면으로 이동 |
| USER | 허용 | 403 | 403 |
| MANAGER | 허용 | 허용 | 403 |
| ADMIN | 허용 | 허용 | 허용 |

`/testdb`는 TestDB 테이블 목록이며, `/admin/members`는 로그인 회원을 관리하는 Member 테이블 목록입니다.

## 7주차 - REST API와 JSON

7주차 회원 관리 REST API(JSON) 완료. PDF 11~28쪽 실습과 32쪽 연습문제를 반영했습니다.

14쪽까지 작성한 `MemberApiController`, `MemberResponse`와 한글 UTF-8·JSON 정렬 설정은 유지하고, 이후 실습을 추가했습니다. API 회원 관리 화면은 교수님 제공 원본을 사용하고, 기존 회원 관리 화면에는 제공된 링크만 추가했습니다.

| PDF | 구현 내용 | 관련 파일 |
| --- | --- | --- |
| 11~14쪽 | 첫 JSON API, 로그인 회원 정보, 비밀번호를 제외한 응답 DTO | [MemberApiController.java](src/main/java/com/example/demo/controller/MemberApiController.java), [MemberResponse.java](src/main/java/com/example/demo/model/dto/MemberResponse.java) |
| 15~18쪽 | No Auth 확인 후 Basic 인증 추가, API 비로그인 401, `/api/**` CSRF 제외 | [SecurityConfig.java](src/main/java/com/example/demo/config/SecurityConfig.java) |
| 21~24쪽 | 관리자 목록·단건 조회·등록, 필수값 검사, 201과 Location | [AdminApiController.java](src/main/java/com/example/demo/controller/AdminApiController.java), [MemberService.java](src/main/java/com/example/demo/model/service/MemberService.java) |
| 25~27쪽 | RoleRequest, 권한 변경 200, 삭제 204, 서비스 검사 재사용 | [RoleRequest.java](src/main/java/com/example/demo/model/dto/RoleRequest.java) |
| 28쪽 | `/admin/members-api` 화면 연결, fetch 요청·응답 기록 | [AdminController.java](src/main/java/com/example/demo/controller/AdminController.java), [members-api.html](src/main/resources/templates/admin/members-api.html) |
| 32쪽 ① | 아이디 또는 이름 부분 검색, `?keyword=student` | [MemberRepository.java](src/main/java/com/example/demo/model/repository/MemberRepository.java) |
| 32쪽 ② | 반복되는 IllegalArgumentException 처리를 한 곳으로 이동, 동일한 400 오류 JSON | [ApiExceptionHandler.java](src/main/java/com/example/demo/controller/ApiExceptionHandler.java) |

32쪽은 완성 코드 대신 구현 힌트를 제공하는 연습문제입니다. `@RequestParam(required = false)`, `findByUsernameContainingOrNameContaining`, `@RestControllerAdvice`와 `@ExceptionHandler(IllegalArgumentException.class)`로 구현했습니다. 공통 오류 처리는 관리자 API에만 적용하여 기존 HTML 화면의 오류 안내를 유지합니다.

### API 주소와 결과

| 메서드 | 주소 | 인증·권한 | 정상 결과 |
| --- | --- | --- | --- |
| GET | `/api/hello` | 누구나 | 200, message·time |
| GET | `/api/me` | 로그인 | 200, 본인 응답 DTO |
| GET | `/api/admin/members` | ADMIN | 200, 응답 DTO 배열 |
| GET | `/api/admin/members?keyword=student` | ADMIN | 200, 아이디·이름 검색 결과 |
| GET | `/api/admin/members/{id}` | ADMIN | 200, 없으면 404 |
| POST | `/api/admin/members` | ADMIN | 201, Location 헤더와 등록 회원 |
| PATCH | `/api/admin/members/{id}/role` | ADMIN | 200, 변경 회원 |
| DELETE | `/api/admin/members/{id}` | ADMIN | 204, 본문 없음 |

- 응답은 `MemberResponse(id, username, name, role)`만 사용하며 password를 포함하지 않습니다.
- 등록 시 5주차 가입 서비스의 BCrypt, 중복 검사, 비밀번호 확인, USER 권한 고정을 재사용합니다.
- 중복·빈 값·비밀번호 확인 불일치·SUPER 권한·관리자 본인 변경·삭제는 400과 `{"error":"안내 메시지"}`로 응답합니다.
- 관리자 API는 USER·MANAGER에게 403이며 서비스의 `@PreAuthorize`도 유지합니다.
- 6주차 연습문제의 MANAGER 권한과 `/testdb` 접근 규칙을 유지합니다.

### 실습 순서와 인증

1. `/api/hello`에서 한글 JSON을 확인합니다.
2. `student1`으로 로그인하고 `/api/me`에서 password가 없는지 확인합니다.
3. `admin`으로 로그인하고 회원 관리의 **API 버전으로 보기**를 누릅니다.
4. API 화면에서 검색, 회원 등록, 권한 변경, 삭제, 9999번 조회를 실행하고 오른쪽 HTTP 기록의 상태 코드를 확인합니다.
5. Postman 등 외부 클라이언트에서는 Basic Auth에 기존 실습 계정과 비밀번호를 지정합니다. JSON 요청 본문은 `Content-Type: application/json`으로 전송합니다.

기존 계정은 `student1 / 123123`, `admin / 1234`를 유지했습니다. PDF 18·27쪽의 `student1 / 1234` 예시는 본인 DB의 비밀번호에 맞춰 `123123`을 사용합니다. 회원 ID도 DB의 실제 번호를 사용합니다.

15·16쪽의 중간 단계에서는 No Auth 요청이 302로 로그인 화면에 이동하지만, 18쪽을 완료한 최종 코드에서는 `/api/me`의 No Auth 요청이 **401, 본문 없음**입니다. Basic 비밀번호 오류도 401이며, 로그인된 브라우저의 세션 요청은 200입니다. HTML 화면은 브라우저 요청(`Accept: text/html`)일 때 기존처럼 로그인 화면으로 이동합니다.

### 등록 요청 예시

```json
{
  "username": "postman1",
  "password": "1234",
  "passwordConfirm": "1234",
  "name": "포스트맨"
}
```

권한 변경 요청은 `{"role":"ADMIN"}`입니다. 응답의 Location 또는 실제 회원 ID를 사용합니다. 등록 JSON에 `"role":"ADMIN"`을 추가해도 요청 DTO에 role이 없어 USER로 가입됩니다.

### 검증 결과

2026-10-21: `./mvnw -o verify`의 기존 테스트 16개 통과. Basic 인증 추가 후 브라우저 동작을 검증하는 두 테스트 클래스에는 `Accept: text/html`을 명시했습니다.

실제 MySQL에서 HTTP 요청 34건으로 No Auth·Basic 인증 성공/실패, 비밀번호 제외, 목록·단건·검색, 201과 Location, 중복·필수값·확인 불일치 400, 권한 변경 200, 본인 보호 400, USER·MANAGER 403, 삭제 204를 확인했습니다. 초기에는 Postman 앱이 없어 동일한 HTTP 요청으로 검증했습니다. 이후 공식 Postman 데스크톱 앱을 설치하고 가입 없이 15·16쪽 실습을 직접 실행했습니다. GET /api/hello의 No Auth 요청은 200 JSON, 18쪽 이전 임시 실행 상태의 GET /api/me는 302와 Location: /login, 자동 이동 시 200 로그인 HTML을 확인했습니다. 확인 후 원래 보안 설정을 복구했고, 최종 서버의 No Auth /api/me는 다시 401임을 Postman에서 확인했습니다. 기존 student1·admin 계정은 유지하고 검증용 회원만 등록·삭제했습니다. 실제 브라우저에서도 제공 화면의 검색·등록 201·변경 200·삭제 204·없는 번호 404·빈칸 400, 새로고침 0회와 JavaScript 오류 0건을 확인했습니다.

## 프로젝트 구조

```text
src/main/java/com/example/demo/
├── DemoApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── DemoController.java
│   ├── MemberController.java
│   ├── AdminController.java
│   ├── MemberApiController.java
│   ├── AdminApiController.java
│   └── ApiExceptionHandler.java
└── model/
    ├── domain/       # Member, TestDB
    ├── dto/          # MemberForm, MemberResponse, RoleRequest
    ├── repository/   # MemberRepository, TestRepository
    └── service/      # MemberService, TestService

src/main/resources/
├── application.properties
├── application-secret.properties.example
├── static/           # CSS, JS, 이미지, 폰트, 상세 페이지
├── public/           # 웹 상세 페이지
└── templates/
    ├── index.html
    ├── hello.html
    ├── testdb.html
    ├── login.html
    ├── signup.html
    ├── mypage.html
    ├── admin/members.html
    ├── admin/members-api.html
    └── error/403.html
```

`application-secret.properties`는 로컬에서 준비하며 GitHub에 업로드하지 않습니다.

## 실행

### VS Code에서 실행

1. `springboot_20220672/demo` 폴더를 엽니다.
2. 실행 및 디버그에서 `Spring Boot-DemoApplication<demo>`를 선택합니다.
3. F5 또는 실행 버튼을 누릅니다.
4. 로컬 실행 설정의 `수업 MySQL 준비` 작업이 Docker와 기존 `mysql-server`를 먼저 준비합니다.
5. `Started DemoApplication`이 표시되면 [메인 화면](http://localhost:8080)에 접속합니다.

VS Code 실행 설정은 로컬 `.vscode` 폴더에 있으며 Git에서 제외됩니다. 다른 PC에서는 DB를 준비한 뒤 아래 Maven 명령으로 실행할 수 있습니다.

### 터미널에서 실행

Java 25와 MySQL이 실행 중인 상태에서, `pom.xml`이 있는 폴더에서 실행합니다.

```sh
./mvnw spring-boot:run
```

Windows에서는 다음 명령을 사용합니다.

```powershell
.\mvnw.cmd spring-boot:run
```

### 로컬 실습 계정

현재 작업 PC의 MySQL에 준비한 수업용 계정입니다. 다른 DB에는 자동 생성되지 않습니다.

| 아이디 | 비밀번호 | 권한 |
| --- | --- | --- |
| student1 | 123123 | USER |
| admin | 1234 | ADMIN |

[로그인](http://localhost:8080/login) · [회원가입](http://localhost:8080/signup) · [내 정보](http://localhost:8080/mypage) · [회원 관리](http://localhost:8080/admin/members)

### 실행 오류 확인

| 증상 | 원인 및 해결 |
| --- | --- |
| `Communications link failure` / `Connection refused` | Docker와 MySQL 실행 여부, 3306 포트 확인 |
| `Unable to determine Dialect without JDBC metadata` | 앞선 DB 연결 오류와 계정 설정 확인 |
| `Access denied for user` | 비밀 설정 파일의 MySQL 아이디·비밀번호 확인 |
| `Port 8080 was already in use` | 기존 서버에 접속하거나 해당 서버를 종료한 뒤 한 번만 실행 |
| 권한 변경 후 메뉴가 그대로 표시됨 | 로그아웃 후 재로그인하여 세션 권한 갱신 |

## 테스트

```sh
./mvnw test
```

- [DemoApplicationTests.java](src/test/java/com/example/demo/DemoApplicationTests.java): 애플리케이션 컨텍스트 실행
- [MemberServiceTests.java](src/test/java/com/example/demo/MemberServiceTests.java): 가입, BCrypt, 중복 검사, 비밀번호 확인
- [LoginFlowTests.java](src/test/java/com/example/demo/LoginFlowTests.java): 로그인·로그아웃, 원래 페이지 복귀, CSRF, remember-me
- [AuthorizationTests.java](src/test/java/com/example/demo/AuthorizationTests.java): 권한별 접근, 관리자 본인 보호, 변경·삭제, 메서드 보안

2026-10-20 검증: `./mvnw -o verify` 테스트 16개 통과, 실패·오류·건너뜀 0, JAR 빌드 성공. 실제 MySQL에서도 student1·admin 로그인, 내 정보, 권한별 접근, 로그아웃 확인.
