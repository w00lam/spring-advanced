# SPRING ADVANCED

## Lv7. 테스트 커버리지

IntelliJ IDEA의 `Run with Coverage`를 사용해 테스트 커버리지를 확인했다. 1차 측정 결과 `client`, `config` 계층의 Line Coverage와 Branch Coverage가 낮아, 해당 영역을 중심으로 테스트 케이스를 추가했다.

### 커버리지 개선 결과

| 측정 단계 | Line Coverage | Condition/Branch Coverage |
| --- | ---: | ---: |
| 1차 측정 | 39% | 26% |
| 2차 측정 | 55% | 61% |

### 1차 커버리지 결과

![Lv7 테스트 커버리지 1차 결과](docs/images/lv7-coverage-first.png)

### 2차 커버리지 결과

![Lv7 테스트 커버리지 2차 결과](docs/images/lv7-coverage-second.png)

### 보강한 테스트

- `WeatherClientTest`: 날씨 조회 성공, 응답 실패, 응답 body null, 빈 배열, 오늘 날짜 데이터 없음 케이스를 검증했다.
- `JwtUtilTest`: 토큰 생성, claim 추출, Bearer 토큰 추출 성공/실패 케이스를 검증했다.
- `AuthUserArgumentResolverTest`: `@Auth`와 `AuthUser` 조합의 정상/오류 케이스, request attribute 기반 `AuthUser` 생성 케이스를 검증했다.
- `AuthServiceTest`, `UserServiceTest`: 서비스 계층의 성공 흐름과 주요 예외 분기를 보강했다.

### 확인한 지표

- Line Coverage: 테스트가 실제로 실행한 소스 코드 라인의 비율을 확인했다.
- Condition/Branch Coverage: 조건식과 분기문의 참/거짓 경로가 테스트되는지 확인했다.
