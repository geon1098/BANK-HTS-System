# 계좌이체 뱅킹 시스템

프로젝트 명: 뱅킹투

Spring Boot로 만든 간단한 은행 API입니다. 회원가입/로그인부터 입출금, 계좌이체, 거래내역 조회까지 구현해봤습니다.
공부하면서 "은행은 돈을 다루니까 보안, 동시성이랑 트랜잭션이 중요하겠지?" 싶어서 그 부분을 신경 써서 만들었습니다.

## 사용 기술

- Java 21
- Spring Boot 3.5
- Spring Security + JWT (jjwt)
- Spring Data JPA
- H2 (인메모리 DB)
- Gradle

## 주요 기능

- 회원가입 / 로그인 (비밀번호는 BCrypt로 암호화, 로그인하면 JWT 토큰 발급)
- 계좌 생성 / 조회 (본인 계좌만 볼 수 있음)
- 입금 / 출금
- 계좌이체
  - 멱등성 키로 같은 요청 두 번 들어와도 한 번만 처리되게 함
  - 이체 취소 기능 (원래 기록을 지우지 않고 반대 거래를 추가하는 방식)
- 거래내역 조회 (페이징)
- 관리자용 전체 계좌 조회 (ROLE_ADMIN만 가능)

## 신경 쓴 부분

- **동시성**: 같은 계좌에 동시에 출금이 들어오면 잔액이 꼬일 수 있어서 비관적 락(`SELECT ... FOR UPDATE`)을 걸었습니다.
- **데드락 방지**: 이체할 때 두 계좌를 잠그는데, 항상 id가 작은 계좌부터 잠그도록 해서 교착상태가 안 생기게 했습니다.
- **트랜잭션**: 이체 도중 잔액이 부족하면 출금/입금이 전부 롤백되도록 했습니다. (반쪽만 처리되면 안 되니까요)
- **거래내역은 지우지 않음**: 이체 취소도 원본을 삭제하는 게 아니라 반대 거래를 새로 쌓는 식(append-only)으로 했습니다. 은행은 기록이 남아야 하니까요.

## 실행 방법

```bash
./gradlew bootRun
```

## 테스트

```bash
./gradlew test
```

이체 성공 시 총액 보존, 잔액 부족 시 롤백, 멱등성 키 중복 처리 방지, 동시성 테스트 등을 작성했습니다.

## curl로 직접 테스트해보기

서버를 켠 상태(`./gradlew bootRun`, 포트 8092)에서 아래 순서대로 따라하면 됩니다.
회원가입 → 로그인 → 계좌 만들기 → 입금 → 이체 → 조회 흐름이에요.

> 참고: DB가 인메모리(H2)라 서버를 새로 켜면 계좌 id가 1번부터 시작합니다.
> 그래서 아래 예시는 첫 계좌를 1번, 두 번째 계좌를 2번이라고 가정했습니다. 다르면 응답에 나온 `accountId`로 바꿔주세요.

### 리눅스 / 맥 (bash)

```bash
# 1. 회원가입
curl -X POST http://localhost:8092/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"1234"}'

# 2. 로그인 → 토큰 받아서 변수에 저장 (jq 필요)
TOKEN=$(curl -s -X POST http://localhost:8092/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"1234"}' | jq -r .accessToken)
echo $TOKEN

# 3. 계좌 두 개 만들기
curl -X POST http://localhost:8092/api/accounts -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8092/api/accounts -H "Authorization: Bearer $TOKEN"

# 4. 1번 계좌에 10000원 입금
curl -X POST http://localhost:8092/api/accounts/1/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":10000}'

# 5. 1번 → 2번으로 3000원 이체 (idempotencyKey는 아무 문자열이나 고유하게)
curl -X POST http://localhost:8092/api/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId":1,"toAccountId":2,"amount":3000,"idempotencyKey":"key-001"}'

# 6. 계좌 조회 (1번은 7000, 2번은 3000이면 성공)
curl http://localhost:8092/api/accounts/1 -H "Authorization: Bearer $TOKEN"
curl http://localhost:8092/api/accounts/2 -H "Authorization: Bearer $TOKEN"

# 7. 1번 계좌 거래내역 조회
curl http://localhost:8092/api/accounts/1/transactions -H "Authorization: Bearer $TOKEN"
```

> 멱등성 확인: 5번 이체를 같은 `idempotencyKey`로 한 번 더 보내면 409 에러가 납니다. (중복 처리 방지)

### 윈도우 (PowerShell)

PowerShell에서 `curl`은 다른 명령이라서 진짜 curl인 `curl.exe`를 써야 합니다. (윈도우10 이상 기본 내장)

```powershell
# 1. 회원가입
curl.exe -X POST http://localhost:8092/api/auth/signup `
  -H "Content-Type: application/json" `
  -d '{"username":"alice","password":"1234"}'

# 2. 로그인 → 토큰 변수에 저장
$token = (curl.exe -s -X POST http://localhost:8092/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"alice","password":"1234"}' | ConvertFrom-Json).accessToken
$token

# 3. 계좌 두 개 만들기
curl.exe -X POST http://localhost:8092/api/accounts -H "Authorization: Bearer $token"
curl.exe -X POST http://localhost:8092/api/accounts -H "Authorization: Bearer $token"

# 4. 1번 계좌에 10000원 입금
curl.exe -X POST http://localhost:8092/api/accounts/1/deposit `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{"amount":10000}'

# 5. 1번 → 2번으로 3000원 이체
curl.exe -X POST http://localhost:8092/api/transfers `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{"fromAccountId":1,"toAccountId":2,"amount":3000,"idempotencyKey":"key-001"}'

# 6. 계좌 조회 (1번은 7000, 2번은 3000이면 성공)
curl.exe http://localhost:8092/api/accounts/1 -H "Authorization: Bearer $token"
curl.exe http://localhost:8092/api/accounts/2 -H "Authorization: Bearer $token"

# 7. 1번 계좌 거래내역 조회
curl.exe http://localhost:8092/api/accounts/1/transactions -H "Authorization: Bearer $token"
```

## API 간단 정리

| 기능 | 메서드 | 경로 |
|------|--------|------|
| 회원가입 | POST | `/api/auth/signup` |
| 로그인 | POST | `/api/auth/login` |
| 계좌 생성 | POST | `/api/accounts` |
| 계좌 조회 | GET | `/api/accounts/{accountId}` |
| 입금 | POST | `/api/accounts/{accountId}/deposit` |
| 출금 | POST | `/api/accounts/{accountId}/withdraw` |
| 이체 | POST | `/api/transfers` |
| 이체 취소 | POST | `/api/transfers/{transferGroupId}/cancel` |
| 거래내역 조회 | GET | `/api/accounts/{accountId}/transactions` |
| 전체 계좌 조회(관리자) | GET | `/api/admin/accounts` |

로그인 이후 요청은 헤더에 `Authorization: Bearer {토큰}` 을 넣어줘야 합니다.

## 트러블슈팅(문제해결)

만들면서 막혔던 것들이랑 어떻게 해결했는지 정리해봤습니다.

### 1. 빌드가 아예 안 됨 - MemberRepository에 메서드가 없다는 에러

`./gradlew build` 했더니 `cannot find symbol: method findByUsername` 이런 에러가 여러 개 떴습니다.
서비스 코드에서는 `memberRepository.findByUsername(...)`, `existsByUsername(...)` 를 쓰고 있는데,
정작 `MemberRepository` 인터페이스는 텅 비어 있었던 게 원인이었어요.

Spring Data JPA는 메서드 이름만 규칙대로 적어주면 쿼리를 알아서 만들어준다는 걸 알고,
인터페이스에 아래처럼 선언만 추가해서 해결했습니다.

```java
Optional<Member> findByUsername(String username);
boolean existsByUsername(String username);
```

### 2. ErrorResponse.of(...) 가 없다는 에러

예외 처리하는 `GlobalExceptionHandler`에서 `ErrorResponse.of(code)` 를 쓰는데 컴파일이 안 됐습니다.
알고 보니 import가 스프링이 기본 제공하는 `org.springframework.web.ErrorResponse`로 되어 있었고,
그건 `of(...)` 같은 메서드가 없는 인터페이스였어요. (내가 원한 건 직접 만든 응답 클래스였는데 잘못 import 됐던 것)

그래서 에러 응답용 record를 직접 만들고 import를 정리해서 해결했습니다.

```java
public record ErrorResponse(String code, String message) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }
}
```

### 3. 테스트 하나만 계속 실패 - "금액은 0보다 커야 합니다"

`./gradlew test` 돌리면 멱등성 테스트 하나만 실패했습니다.
로그를 따라가 보니 테스트 헬퍼에서 잔액 0원짜리 계좌를 만들려고 `deposit(0)`을 호출하는데,
입금 금액은 0보다 커야 한다는 도메인 규칙(`@Positive`) 때문에 예외가 터지고 있었어요.

규칙 자체는 맞는 거라서 코드를 바꾸지 않고, 테스트 쪽에서 잔액이 0이면 입금을 건너뛰도록 고쳤습니다.

```java
if (balance > 0) {
    accountService.deposit(account.getId(), BigDecimal.valueOf(balance), username);
}
```

### 4. 작은 오타

`TransactionResponse`의 필드 이름이 `perfromedBy`로 오타가 나 있어서 `performedBy`로 고쳤습니다.
컴파일은 됐지만 API 응답 JSON에 그대로 노출되는 부분이라 그냥 두면 안 될 것 같아서 같이 정리했어요.

## 앞으로 해보고 싶은 것

- DB를 H2 말고 MySQL/PostgreSQL로 바꿔보기
- 이체 한도 / 일일 한도 같은 정책 추가
- 이체 내역(거래 기록) 조회 및 정렬 (보낸 사람, 받은 사람, 금액, 잔액 변동, 거래 유형(입금/출금/이체), 거래 일시를 기록하는 아키텍처를 추가)
- 페이징(Pagination) 및 필터링: 거래 내역이 만 개, 십만 개가 되었을 때 한 번에 로딩하면 서버가 터집니다. 10개씩 끊어서 보여주는 페이징 처리와 '최근 1달', '입금만 보기' 같은 필터링 기능
- 프론트 화면 React.js로 SPA 사용자에게 편한 속도를 빠르게 만들기
- 낙관적 락 (Optimistic Lock) 구현해보기
- Redis 분산 락 (Distributed Lock) 구현해보기
- 실제 서비스처럼: 대외 기관 연동 시뮬레이션(WebClient / FeignClient) 
타행 이체 기능을 만든다고 가정하고, 가상의 타행 서버 API(쉽게 말해 별도의 더미 컨트롤러나 Mock 서버)를 호출하는 로직을 만들어 봅니다.

WebClient나 OpenFeign을 사용해 외부 HTTP 통신을 보내고, 만약 상대방 서버가 응답이 없거나 에러가 났을 때 내 서버의 이체 처리를 어떻게 취소(Rollback)할 것인지 예외 처리 전략


