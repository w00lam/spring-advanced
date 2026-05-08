# Lv6. 개선 과제 회고

## 1. 문제 선정 과정

필수 과제를 진행한 뒤 코드 전반을 다시 살펴보며 개선 가능성이 있는 지점을 찾았다. 후보로는 검증 실패 응답 형식 불일치, 회원가입 비밀번호 검증 누락, 불필요한 JPA 어노테이션 사용, API 경로명 개선, 서비스의 다중 Repository 의존성, 엔티티 생성 방식 개선 등이 있었다.

그중 이번 개선 과제에서는 영향 범위가 명확하고 변경 전후를 설명하기 쉬운 세 가지를 선택했다.

- `@Valid` 검증 실패 시 기존 커스텀 예외 응답과 다른 형식의 응답이 반환되는 문제
- 비밀번호 변경 요청에는 비밀번호 형식 검증이 있지만 회원가입 요청에는 없는 문제
- `LocalDateTime` 필드에 `@Temporal`이 불필요하게 선언된 문제

## 2. 문제 정의

### 문제 1. Validation 실패 응답 형식 불일치

프로젝트의 커스텀 예외는 `GlobalExceptionHandler`를 통해 다음과 같은 응답 구조로 반환된다.

```json
{
  "status": "BAD_REQUEST",
  "code": 400,
  "mes${DB_USERNAME}ge": "에러 메시지"
}
```

하지만 `@Valid` 검증 실패로 발생하는 `MethodArgumentNotValidException`은 별도로 처리하지 않아 Spring 기본 에러 응답 형식이 반환될 수 있었다. 같은 잘못된 요청이라도 예외 발생 경로에 따라 응답 형식이 달라지는 문제가 있었다.

### 문제 2. 회원가입 비밀번호 검증 누락

비밀번호 변경 요청인 `UserChangePasswordRequest`에는 새 비밀번호가 8자 이상이고 숫자와 대문자를 포함해야 한다는 검증 규칙이 있다. 반면 회원가입 요청인 `SignupRequest`에는 `@NotBlank`만 있어, 회원가입 시에는 같은 비밀번호 정책이 적용되지 않았다.

사용자 계정 생성 시점과 비밀번호 변경 시점의 검증 정책이 다르면 서비스 정책이 일관되지 않고, 약한 비밀번호로 가입한 뒤 이후 변경 시점에만 강한 규칙을 요구하는 어색한 흐름이 생긴다.

### 문제 3. `LocalDateTime` 필드의 불필요한 `@Temporal` 사용

공통 시간 엔티티인 `Timestamped`에서 `createdAt`, `modifiedAt` 필드는 `LocalDateTime` 타입이다. 그런데 해당 필드에 `@Temporal(TemporalType.TIMESTAMP)`가 함께 선언되어 있었다.

`@Temporal`은 `java.util.Date`나 `Calendar` 타입을 매핑할 때 사용하는 어노테이션이다. `LocalDateTime`은 JPA가 직접 지원하는 타입이므로 `@Temporal`이 필요하지 않다. 불필요한 어노테이션은 코드를 읽는 사람에게 잘못된 의도를 전달할 수 있다.

## 3. 해결 과정

### 1. Validation 예외 응답 형식 통일

`GlobalExceptionHandler`에 `MethodArgumentNotValidException` 처리 메서드를 추가했다. 검증 실패 시 첫 번째 필드 에러 메시지를 꺼내 기존 `getErrorResponse()` 메서드를 통해 동일한 응답 구조로 반환하도록 했다.

이를 통해 커스텀 예외와 Bean Validation 예외가 같은 응답 형식을 사용하게 되었다.

### 2. 회원가입 비밀번호 검증 추가

`SignupRequest`의 `password` 필드에 `@Pattern`을 추가했다.

검증 규칙은 비밀번호 변경 요청과 동일하게 설정했다.

```text
8자 이상
숫자 포함
대문자 포함
```

이제 회원가입과 비밀번호 변경 모두 동일한 비밀번호 정책을 따른다.

### 3. 불필요한 `@Temporal` 제거

`Timestamped`의 `createdAt`, `modifiedAt` 필드에서 `@Temporal(TemporalType.TIMESTAMP)`를 제거했다. `@CreatedDate`, `@LastModifiedDate`, `@Column`은 유지하여 기존 auditing 동작은 그대로 보존했다.

## 4. 검증 결과

전체 테스트를 실행해 기존 동작이 깨지지 않는지 확인했다.

```powershell
.\gradlew.bat test
```

결과:

```text
BUILD SUCCESSFUL
```

## 5. 개선 후 효과

- 잘못된 요청에 대한 에러 응답 형식이 더 일관되게 유지된다.
- 회원가입과 비밀번호 변경의 비밀번호 검증 정책이 동일해졌다.
- `LocalDateTime`에 맞지 않는 JPA 어노테이션을 제거해 매핑 의도가 더 명확해졌다.
- 작은 수정이지만 검증, 예외 처리, 엔티티 매핑의 의미가 더 분명해졌다.

## 6. 회고

이번 개선을 통해 단순히 기능이 동작하는지만 보는 것이 아니라, 같은 종류의 실패가 같은 방식으로 표현되는지, 검증 정책이 요청 흐름마다 일관적인지, 어노테이션이 타입과 목적에 맞게 사용되는지를 함께 살펴보는 것이 중요하다는 점을 확인했다.

특히 `@Valid`를 적용하는 것만으로는 충분하지 않았다. 검증 실패가 발생했을 때 클라이언트가 받는 응답까지 프로젝트의 기존 규칙과 맞아야 실제 API 품질이 좋아진다. 또한 작은 어노테이션 하나도 코드의 의도를 설명하는 역할을 하므로, 필요하지 않은 선언은 제거하는 편이 유지보수에 더 도움이 된다고 느꼈다.
