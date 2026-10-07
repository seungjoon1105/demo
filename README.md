# 4주차·5주차·6주차 자바웹프로그래밍 실습

4주차 MySQL 연동·사용자 정보 출력, 5주차 로그인·로그아웃·BCrypt 암호화와 6주차 권한(ROLE) 접근 제어·관리자 페이지를 구현했습니다. 각 주차 연습문제도 포함합니다. 프로젝트 설명과 실행·실습 안내는 이 README.md에서 관리합니다.

## 확인한 수업 자료

- `4주차 데이터베이스 연동 및 테스트(수정됨).pdf`: 1~33쪽 전체.

- `5주차 로그인 로그아웃 및 암호화(수정됨).pdf`: 1~30쪽 전체.
- `5주차 로그인 로그아웃 및 암호화(수정됨)-2.pdf`: 1~30쪽 전체.
- `5주차_추가자료`, `5주차_추가자료-2`: 로그인·가입 화면, 네비게이션 교체부분, 의존성, .gitignore.
- `6주차 권한 관리 및 관리자 페이지(수정됨).pdf`: 1~31쪽 전체.
- `6주차_제공코드`: index 교체부분, mypage, admin/members, error/403 및 관리자 권한 부여 SQL 내용.

5주차 PDF 12쪽 구조 그림과 13~21쪽 package 선언을 기준으로 파일을 배치했습니다. 6주차는 9쪽 안내대로 기존 엔티티·DTO·리포지토리를 유지하고 컨트롤러·서비스·보안 설정에 기능을 추가했습니다. 직접 꾸민 포트폴리오에는 자료의 네비게이션 교체부분을 반영했습니다.

## 프로젝트 구조

실제 Maven 프로젝트는 `springboot_20220672/demo`입니다.

```text
demo/
├── README.md
├── pom.xml
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── DemoController.java
│   │   ├── MemberController.java
│   │   └── AdminController.java
│   └── model/
│       ├── domain/
│       │   ├── TestDB.java
│       │   └── Member.java
│       ├── dto/
│       │   └── MemberForm.java
│       ├── repository/
│       │   ├── TestRepository.java
│       │   └── MemberRepository.java
│       └── service/
│           ├── TestService.java
│           └── MemberService.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-secret.properties         # 로컬 비밀 설정, Git 제외
│   ├── application-secret.properties.example # 비밀값 없는 설정 예시
│   ├── static/                               # 기존 CSS·JS·이미지·폰트
│   ├── public/                               # 기존 상세 페이지
│   └── templates/
│       ├── index.html
│       ├── hello.html
│       ├── testdb.html
│       ├── login.html
│       ├── signup.html
│       ├── mypage.html
│       ├── admin/members.html
│       └── error/403.html
└── src/test/                                 # 실습 기능 검증
```

보안 설정은 `src/main/java/com/example/demo/config/SecurityConfig.java` 한 곳에서 수정합니다. PDF의 config는 이 자바 소스 경로 아래의 패키지입니다. 최상위 `demo/config/SecurityConfig.java` 중복 사본은 삭제했습니다. 별도 SQL 폴더·SQL 파일·주차별 MD 파일은 사용하지 않습니다.

## 실행 준비: 교수님과 동일한 MySQL 환경

기본 실행 DB를 MySQL로 변경했습니다. H2는 자동 테스트에만 사용합니다. 이전 H2의 회원 데이터는 MySQL로 자동 이동되지 않으므로 MySQL에서 실습 계정을 다시 가입해야 합니다. 기존 H2 데이터 파일은 보존했습니다.

1. MySQL 서버를 실행합니다.
2. VS Code MySQL 확장에서 수업 때 사용한 서버와 계정으로 접속합니다.
3. 앱이 사용할 `spring` 데이터베이스가 있는지 확인합니다. 다른 이름을 사용한다면 `application.properties`의 URL도 같은 이름으로 맞춥니다.
4. `src/main/resources/application-secret.properties`에 실제 MySQL 계정을 입력합니다. 다른 PC에서는 `.example` 파일을 복사해 준비합니다.

```properties
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
app.security.remember-me-key=YOUR_RANDOM_SECRET
```

`YOUR_...` 부분은 실제 값으로 교체합니다. 현재 작업 PC에는 기존 4주차 프로젝트의 MySQL 계정 설정을 비밀 파일에 옮겨 두었습니다. 해당 계정이 현재 MySQL 서버에서 유효한지는 접속 후 확인해야 합니다. 기존 remember-me 비밀키는 유지했습니다. 계정 파일은 Git에서 제외됩니다.

`application.properties`의 기본 연결 대상:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/spring?serverTimezone=Asia/Seoul
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.config.import=optional:classpath:application-secret.properties
```

JPA는 연결된 DB 안에 `member`, `testdb` 테이블을 생성·갱신합니다. DB와 접속 가능한 MySQL 계정은 먼저 준비해야 합니다.

Java 25 환경에서 실행합니다.

```sh
cd /Users/joo/springboot_20220672/demo
./mvnw spring-boot:run
```

브라우저: http://localhost:8080

`mysql` 프로필 옵션은 이제 필요하지 않습니다. DB 서버 연결 실패 또는 계정 오류가 나오면 서버 주소·DB 이름·비밀 설정을 확인합니다.

## 4주차 진행 내용

| PDF 페이지 | 내용 및 반영 |
| --- | --- |
| 1~7 | 데이터베이스·MySQL·JPA 개념 및 주차 목표 확인 |
| 8~10 | Spring Data JPA와 MySQL 커넥터 의존성 확인 |
| 11~16 | MySQL 설치·접속·spring DB 생성·application.properties·VS Code MySQL 확장 사용 순서 정리 |
| 17~20 | 구조 그림대로 controller, model/domain, model/repository, model/service 패키지 분리 |
| 21~24 | TestDB 엔티티·TestRepository·TestService 및 DemoController의 `/testdb` 조회 |
| 25~27 | testdb 테이블 자동 생성, MySQL 확장에서 직접 INSERT 후 화면 확인 |
| 28~29 | findAll 전체 조회, users 모델 속성, th:each로 목록 출력 |
| 30 | README 갱신. Git 커밋·푸시는 수행하지 않음 |
| 31~32 | 연습문제: age·gender 컬럼 추가, 화면에 아이디·이름·나이·성별 출력 |
| 33 | Q&A 확인 |

4주차 PDF 19쪽의 구조 그림은 `model/repository`입니다. TestRepository는 이 패키지에서 `@Repository`와 JpaRepository를 사용하고, TestService는 PDF 23쪽처럼 `@Autowired` 필드 주입을 사용합니다. `/testdb`는 `findAll()` 조회만 수행합니다. 기존 자동 홍길동 저장 코드는 제거했습니다.

PDF 24쪽의 `Testservice`는 실제 클래스 선언인 `TestService`와 대소문자가 다른 오탈자이므로 컴파일되는 선언 이름 `TestService`를 사용합니다. 5주차 16쪽 비교 표에 TestDB ID가 직접 입력으로 설명되지만, 4주차 21쪽 실제 엔티티 코드의 `@GeneratedValue(IDENTITY)`를 유지합니다.

MySQL 서버·계정 준비는 PDF 11~16쪽 순서입니다. macOS는 자료에 안내된 Docker 방식으로 MySQL 서버를 준비하고 VS Code MySQL 확장에서 접속합니다. 프로젝트의 코드만으로 MySQL 서버가 설치되지는 않습니다. 접속 후 DB가 없을 때 다음을 실행합니다.

```sql
SHOW DATABASES;
CREATE DATABASE spring;
FLUSH PRIVILEGES;
```

서버를 실행하여 JPA가 `testdb` 테이블과 age·gender 컬럼을 생성·갱신한 뒤, VS Code MySQL 확장에서 해당 테이블의 INSERT 쿼리를 실행합니다. 아래는 32쪽 그림의 이름·나이·성별을 입력하는 예시입니다. 기존 데이터가 있다면 중복 삽입하지 말고 필요한 행만 추가·수정합니다.

```sql
INSERT INTO testdb (name, age, gender) VALUES
('홍길동', 25, '남'),
('아저씨', 40, '남'),
('김영희', 30, '여'),
('이철수', 22, '남');
SELECT id, name, age, gender FROM testdb;
```

최종 6주차 접근 규칙에서는 ADMIN 또는 MANAGER로 `/testdb`에 접속하여 DB에 입력한 네 컬럼의 값을 확인합니다. 데이터를 넣기 전에는 헤더만 표시하며 조회 요청이 DB에 회원을 자동 생성하지 않습니다.

## 5주차 진행 내용

| PDF 페이지 | 내용 및 반영 |
| --- | --- |
| 1~7 | 웹 보안·인증, BCrypt와 솔트, 세션 기반 로그인 개념 및 주차 목표 확인 |
| 8~9 | 기존 TestDB 목록과 MVC 구조 확인. 제공 login·signup 화면 및 개인 포트폴리오의 네비게이션 ①~④ 반영 |
| 10~11 | Spring Security, Thymeleaf Security extras 의존성. 컨트롤러 앞에서 필터 체인으로 인증·인가 처리 |
| 12~15 | PDF 패키지 구조, SecurityConfig, 공개 URL·정적 리소스 허용, 나머지 인증 요구, 로그인·가입 GET 컨트롤러 |
| 16~18 | Member 엔티티, MemberRepository, MemberForm DTO. username 중복 금지·ID 자동 증가. DTO에는 role을 넣지 않음 |
| 19~20 | MemberService 가입·중복 검사·BCrypt 저장·USER 지정, 가입 POST와 성공·오류 안내 |
| 21~24 | UserDetailsService DB 조회, Spring Security의 비밀번호 비교, 실패 안내·원래 요청 복귀, 사용자 아이디 표시 |
| 25~26 | POST 로그아웃, 세션 무효화, JSESSIONID·remember-me 삭제, CSRF 및 오류 해결 항목 확인 |
| 27 | MySQL 계정·remember-me 키를 비밀 설정에 분리, .gitignore 적용, README 정리 |
| 28~29 | 연습문제: remember-me 7일, 비밀번호 확인 불일치 메시지·저장 차단 |
| 30 | Q&A 확인 |

회원가입 메서드는 PDF 방식의 중복 검사·비밀번호 확인·BCrypt·USER 지정으로 작성했습니다. 아이디 길이 등은 제공 signup.html의 입력 제한을 그대로 사용합니다. 제가 별도로 추가했던 서버 입력 길이 제한은 제거했습니다.

`MemberService`는 `@Autowired` 필드 주입을 사용하고 `UserDetailsService`를 구현합니다. 비밀번호 비교는 직접 구현하지 않고 Spring Security가 `PasswordEncoder.matches()`로 처리합니다. `.roles(member.getRole())`가 USER를 ROLE_USER로 변환합니다.

제공 로그인·회원가입 HTML은 추가자료 원본과 같습니다. 메인 포트폴리오는 PDF 안내대로 네비게이션 부분만 반영했습니다. 추가 디자인 보정은 제거했습니다.

5주차에서 USER도 `/testdb`에 접근하던 규칙은 6주차 연습문제 이후 변경되었습니다. 최종 코드에서는 USER의 원래 요청 복귀·로그인 유지 실습을 `/mypage`로 확인합니다.

## 6주차 진행 내용

| PDF 페이지 | 내용 및 반영 |
| --- | --- |
| 1~7 | 접근 제어 실패·IDOR·권한 상승, RBAC와 최소 권한, 화면·URL·메서드 보안 개념 및 주차 목표 확인 |
| 8~10 | 기존 index에 내 정보·관리자 메뉴·역할 배지. 제공 mypage·admin/members·error/403 화면 배치 |
| 11~13 | Principal 아이디로 본인 회원 조회. DB role과 로그인 세션 authorities를 각각 표시 |
| 14 | 정상 회원가입 후 MySQL에서 admin의 role만 UPDATE하는 실습 순서 정리 |
| 15~18 | AdminController 신규, 회원 ID 순 조회, 허용 권한 목록, 권한 변경·삭제 POST, redirect와 flash 메시지 |
| 19~20 | 메뉴 숨김만으로는 URL 직접 접근을 막을 수 없다는 취약 상태·3단계 접근 제어 설명 확인 |
| 21~23 | `/admin/**` ADMIN 전용. 비로그인 로그인 이동, 로그인한 비관리자는 403. 제공 오류 화면 연결 |
| 24~25 | `@EnableMethodSecurity`, 변경·삭제의 `@PreAuthorize`. URL 규칙을 잠시 해제하는 이중 잠금 실험 방법 정리 |
| 26~27 | 역할 표현식과 전체 테스트·오류 해결 항목 확인 |
| 28 | README 갱신, 비밀 설정 제외. Git 커밋·푸시는 아직 수행하지 않음 |
| 29~30 | 연습문제: 관리자 본인 변경·삭제 거부, MANAGER 추가, `/testdb` ADMIN·MANAGER 제한, USER에게 회원목록 메뉴 숨김 |
| 31 | Q&A와 다음 주 REST API 예고 확인. 다음 주 기능은 이번 구현 범위에 포함하지 않음 |

`AdminController`는 교수님 코드대로 `@Autowired` 필드 주입, `@RequestMapping("/admin")`, GET 회원 조회와 두 POST 메서드를 사용합니다. 서비스는 `findAll(Sort.by("id"))`, `findById`, `save`, `delete`를 사용합니다. 본인 보호는 연습문제 힌트대로 각 변경·삭제 메서드 안에서 `SecurityContextHolder`의 로그인 아이디와 비교합니다.

최종 구현은 30쪽 연습문제까지 적용한 상태입니다. 따라서 18쪽의 중간 단계에서 거부되던 MANAGER는 최종 코드에서는 허용됩니다. 19쪽의 권한 상승 가능 상태와 25쪽의 URL 잠금 해제 상태는 학습 중간 실험이며 기본 실행에는 두 잠금이 모두 적용됩니다.

| 상태 | `/mypage` | `/testdb` | `/admin/**` |
| --- | --- | --- | --- |
| 비로그인 | 로그인 화면 | 로그인 화면 | 로그인 화면 |
| USER | 허용 | 403 | 403 |
| MANAGER | 허용 | 허용 | 403 |
| ADMIN | 허용 | 허용 | 허용 |

`/testdb`는 4주차 TestDB 테이블 목록이고 `/admin/members`는 로그인 회원인 Member 테이블 목록입니다.

## 관리자 로그인: PDF 14쪽 순서

1. MySQL로 앱을 실행하고 `/signup`에서 `admin / 1234 / 관리자`로 가입합니다. 비밀번호 확인도 1234입니다. 이미 가입되어 있다면 기존 계정·비밀번호를 사용합니다.
2. VS Code MySQL 확장에서 **앱이 연결한 spring DB**를 선택하고 쿼리 실행 화면에 아래 SQL을 입력·실행합니다. 프로젝트에 별도 SQL 파일을 만들지 않습니다.

```sql
SELECT id, username, name, role FROM member;
UPDATE member SET role = 'ADMIN' WHERE username = 'admin';
SELECT id, username, name, role FROM member WHERE username = 'admin';
```

3. UPDATE 1건, 조회 결과 ADMIN을 확인합니다. 0건이면 해당 MySQL DB에서 admin으로 가입했는지 확인합니다.
4. 사이트에서 로그아웃하고 admin으로 다시 로그인합니다.
5. 메인에 ADMIN 배지와 빨간 관리자 메뉴가 나타나고, 내 정보의 시큐리티 권한이 `[ROLE_ADMIN]`이면 성공입니다.

아이디 이름이 admin이어도 가입 직후 권한은 USER입니다. SQL로 role을 변경해야 ADMIN입니다. SQL 변경 뒤에도 기존 로그인 세션은 ROLE_USER일 수 있으므로 새로고침만 하지 말고 로그아웃·재로그인합니다. DB에 평문 비밀번호를 INSERT하지 않습니다.

## 직접 검증할 순서

일반 브라우저 창에는 admin, 시크릿 창에는 student1을 로그인하면 비교하기 쉽습니다.

1. **가입**: `student1 / 123123 / 학생`으로 가입합니다. 비밀번호 확인을 다르게 넣으면 불일치 안내와 저장 차단, 같은 아이디로 다시 가입하면 중복 안내를 확인합니다.
2. **로그인**: 틀린 비밀번호로 오류 안내, 올바른 비밀번호로 사용자 이름·로그아웃 표시를 확인합니다. 비로그인으로 `/mypage`에 먼저 접속한 뒤 로그인하면 해당 화면으로 복귀합니다.
3. **USER 차단**: student1로 로그인하여 USER 배지·내 정보 메뉴를 확인합니다. `/admin/members`와 `/testdb`를 주소창에 직접 입력하면 403이고 아이디·ROLE_USER·요청 주소가 표시됩니다.
4. **내 정보**: `/mypage?id=다른회원번호`를 입력해도 Principal로 조회한 본인 정보가 나옵니다.
5. **관리자**: 위 관리자 준비를 완료한 admin으로 회원 관리에 접속하여 ID 순 목록을 확인합니다.
6. **권한 변경**: admin으로 student1을 ADMIN으로 바꿉니다. 이미 로그인한 student1의 내 정보에는 DB ADMIN과 세션 ROLE_USER가 다르게 보일 수 있습니다. student1을 재로그인하면 관리자 메뉴가 나타납니다. 실습 뒤 원래 admin에서 student1을 USER로 복구합니다.
7. **본인 보호**: admin 자신의 행에서 권한 변경 또는 삭제를 누르면 “자기 자신의 권한 변경·삭제는 할 수 없습니다.”가 나오고 DB가 유지됩니다.
8. **MANAGER**: admin으로 student1을 MANAGER로 바꾸고 student1을 재로그인합니다. MANAGER 배지·회원목록 메뉴, `/testdb` 허용과 `/admin/members` 403을 확인합니다.
9. **허용 목록**: 개발자 도구로 select의 값을 SUPER로 바꿔 보내면 허용되지 않는 권한 안내가 나오고 DB가 바뀌지 않습니다.
10. **삭제**: 별도 `deleteuser / 123123 / 삭제테스트` 회원을 가입한 뒤 admin으로 삭제합니다. 목록에서 사라지고 새 로그인은 실패합니다.
11. **로그인 유지**: 체크 후 로그인하여 개발자 도구의 remember-me 쿠키 7일 만료를 확인합니다. JSESSIONID만 삭제한 뒤 `/mypage`에 접속하면 인증이 복원됩니다.
12. **로그아웃**: 버튼을 누르면 안내가 표시되고, `/mypage` 재접속은 로그인 화면으로 이동합니다.

권한은 로그인할 때 세션에 복사하는 PDF 모델입니다. 변경된 권한은 로그아웃 후 재로그인하여 확인합니다. 계정 삭제가 기존 로그인 세션을 즉시 종료하는 기능은 이 실습에 포함하지 않습니다.

## PDF 25쪽 이중 잠금 실험

기본 최종 코드는 URL 규칙과 메서드 보안을 모두 사용합니다. 로컬에서 실험을 직접 해보려면:

1. SecurityConfig에서 `.requestMatchers("/admin/**").hasRole("ADMIN")` 한 줄만 잠시 주석 처리하고 서버를 재시작합니다.
2. USER로 `/admin/members`를 열면 목록은 보입니다.
3. 변경·삭제를 전송하면 서비스의 `@PreAuthorize`가 403으로 거부하고 DB가 유지되는지 확인합니다.
4. 주석을 원래대로 복구하고 서버를 재시작합니다. USER의 관리자 목록 접근도 다시 403이어야 합니다.

## 검증 결과와 외부 준비 상태

2026-10-07: 첨부된 4주차 33쪽·5주차 30쪽·6주차 31쪽 전체 및 제공 자료를 대조하고 PDF 방식으로 재정리한 뒤 검증했습니다.

```sh
./mvnw -o clean verify
```

16개 테스트 통과, 실패·오류·건너뜀 0, JAR 빌드 성공.

- TestDB age·gender 저장·목록 출력, 빈 DB 조회 시 자동 INSERT 없음.
- BCrypt·솔트·회원 중복·비밀번호 확인·가입 USER 고정.
- 로그인 성공·실패·원래 요청 복귀·CSRF·로그아웃·remember-me.
- USER/MANAGER/ADMIN URL 접근·메뉴·역할 배지·본인 정보 조회.
- 관리자 권한 변경·삭제·본인 보호·잘못된 권한·없는 회원 처리.
- URL 필터를 거치지 않는 직접 서비스 호출에서도 USER·MANAGER 변경·삭제 차단.
- login·signup·mypage·admin/members·error/403 HTML 5개는 제공 자료와 동일함을 확인.

자동 테스트는 별도 H2 메모리 DB로 수행했습니다. 현재 로컬 3306 포트의 MySQL 실행은 확인되지 않았고 실제 MySQL 서버·계정 연결은 아직 검증하지 못했습니다. 기본 MySQL 설정과 코드·빌드는 준비된 상태이며, 위 실행 준비의 서버·계정을 설정한 뒤 사이트에서 실습합니다. 이전 H2에서 하던 실행·권한 변경 안내는 현재 기본 실행에 적용하지 않습니다.

Git 커밋·푸시는 수행하지 않았습니다. `application-secret.properties`와 이전 로컬 DB 파일은 .gitignore로 제외합니다.
