# Changelog

백엔드 주요 변경 이력을 날짜 기준으로 기록합니다.

## 2026-04-08

### Added

- 튜터링 row와 Google Calendar 이벤트를 1:1로 연결할 수 있도록 캘린더 event id, html link, sync 상태, 마지막 오류, 마지막 동기화 시각 메타 필드를 추가했습니다.
- 관리자 운영 수익 분석을 위한 `/api/admin/finance/overview`, `/api/admin/finance/details` API와 AWS Cost Explorer, OpenAI Costs API, USD-KRW 환율 API를 묶어 읽는 비용 집계 서비스를 추가했습니다.
- 운영 수익 분석용 `admin_finance_daily_snapshot`, `admin_finance_daily_breakdown`, `admin_finance_sync_job`, `payment_ledger_event` 저장 모델과 `/api/admin/finance/sync` 수동 동기화 API를 추가했습니다.

### Changed

- `/open-api/reset-password`를 추가해 아이디·이메일·인증번호를 확인한 뒤 BASIC 계정의 임시 비밀번호를 새로 발급하고 메일로 보내도록 정리했습니다.
- `/open-api/login` 구현 메서드에 요청 바디 바인딩을 명시해, 로그인 실패 시 기존 `ErrorResponse.message`가 프론트에서 안정적으로 소비될 수 있는 형태를 유지하도록 정리했습니다.
- 튜터링 예약 생성 시 `WAIT` 상태의 임시 Google Calendar 일정을 만들고, `CONFIRM`/`CANCEL`/삭제 시 같은 이벤트를 업데이트 또는 제거하도록 비동기 캘린더 연동 흐름을 추가했습니다.
- 관리자용 `POST /api/tutoring/{tutoringId}/calendar-sync`를 추가해 실패한 튜터링 일정도 현재 상태 기준으로 다시 캘린더와 동기화할 수 있게 정리했습니다.
- Google Calendar 연동은 기존 Google OAuth client 설정을 재사용하고, 운영 env의 refresh token으로 access token을 갱신해 기본 캘린더(`primary`)에 일정을 쓰도록 정리했습니다.
- 운영 수익 분석은 외부 API 실시간 조회 대신 자정 이후 스케줄러와 수동 sync가 DB 일별 스냅샷을 적재하고, `/api/admin/finance/overview`와 `/details`는 저장된 snapshot/ledger 데이터만 조회하도록 정리했습니다.

## 2026-04-07

### Changed

- 관리자 메일 전송 요청마다 최근 작업 row를 저장하고, 비동기 SMTP 발송 결과에 따라 `QUEUED/SENT/FAILED` 상태와 오류 메시지를 갱신할 수 있게 정리했습니다.
- `/api/mail` 관리자 메일 발송은 수신자/본문/첨부 검증 이후 실제 SMTP 전송을 백그라운드 executor로 넘기도록 바꿔, 프론트 요청이 메일 서버 응답까지 길게 대기하지 않게 정리했습니다.
- `/api/mail` 관리자 메일 경로를 inline 이미지 CID 전송까지 지원하도록 확장해, 메일 본문 HTML 안에 삽입한 이미지를 실제 이메일에서도 같은 위치에 표시하고 일반 첨부파일과 함께 검증·전송하도록 정리했습니다.
- 일반 이메일 회원가입의 인증 메일 발송을 비동기 이벤트에서 동기 처리로 전환해, SMTP 실패가 더 이상 200 성공처럼 숨겨지지 않고 API 응답과 로그에 그대로 반영되도록 정리했습니다.
- `/open-api/register`, `/open-api/verify-email`, `/open-api/verify-num` 구현 메서드에 요청 바디 바인딩을 명시하고, 인증번호 검증 실패를 `미발급`, `불일치`, `만료` 성격의 메시지로 더 정확히 전달하도록 정리했습니다.
- 회원가입 성공 시 사용한 이메일 인증번호를 즉시 소비하도록 바꿔, 같은 인증번호가 계속 재사용되지 않게 정리했습니다.
- `/api/mail`의 관리자 메일 발송 경로에서 수신자 이름 누락 같은 런타임 예외도 안전하게 처리하고, SMTP 발신자 헤더를 기본 계정으로 명시해 메일 전송 500 가능성을 줄이도록 보강했습니다.
- `/api/mail`을 `multipart/form-data` 기반으로 확장해 이미지와 일반 문서 파일을 실제 이메일 첨부로 보낼 수 있게 하고, 첨부 개수·총 용량·허용 형식을 서버에서 검증하도록 정리했습니다.

## 2026-04-05

### Added

- 헤더 GNB 설정을 저장하는 `header_navigation_page` 모델과 관리자/공개 조회 API를 추가했습니다.
- Google authorization code를 서버에서 교환하는 `POST /open-api/login/google` API와 Google OAuth code exchange 서비스를 추가했습니다.
- Google OAuth 환경변수 예시를 위한 백엔드 `.env.example` 파일을 추가했습니다.

### Changed

- 헤더 메뉴 설정이 비어 있을 때 기존 운영 중 하드코드 GNB를 fallback 데이터로 sanitize 하도록 정리했습니다.
- JWT admin 권한 판정을 사용자 `authority` 기준으로 통일해 `/api/admin/**` 요청이 stale `roles` 값 때문에 403 되지 않도록 수정했습니다.
- 소셜 온보딩용 `/api/user/social/{userId}`가 요청자 본인 또는 관리자만 수정할 수 있도록 권한 검증과 필수 입력 검증을 보강했습니다.
- 소셜 로그인 HTTP 클라이언트가 OAuth access token 원문을 서버 로그에 남기지 않도록 정리했습니다.

## 2026-04-04

### Added

- 홈 레이아웃 빌더용 `home_layout_page` 저장 모델과 관리자/공개 API를 추가했습니다.
- 강의 레슨 단위 진도 저장 모델과 `/api/video/progress/*` API를 추가했습니다.
- 강의 작성/수정에서 동영상 파일 업로드를 위한 `POST /api/video-file` API를 추가했습니다.
- 어드민 메일 보내기용 `POST /api/mail` API와 전용 안내 메일 템플릿을 추가했습니다.
- 튜터링 메일 양식에 고정 수신 이메일과 발송 대상 제어 기능을 추가했습니다.
- 구독 플랜과 사용자 구독 스냅샷에 질문/튜터링/강의 무한 권한 필드를 추가했습니다.
- 사용자 엔티티에 세션 버전을 추가해 새 로그인 시 기존 access token을 즉시 무효화할 수 있게 했습니다.
- 합격 예측 계산기 추천학교 설정용 `calculator_recommendation_page` 저장 모델과 관리자/공개 API를 추가했습니다.
- 합격 예측 계산기 설정 JSON에 그룹/과목/허용 레벨 규칙을 포함할 수 있도록 그룹 DTO를 추가했습니다.
- 관리자용 회원 사용량 조회 API와 학생별 강의 진도/질문/튜터링/마지막 접속 집계 DTO를 추가했습니다.

### Changed

- `/api/question`이 일반 사용자에게는 본인 질문만, 어드민에게는 전체 질문을 반환하도록 정리했습니다.
- 강의 수정 API를 nested sync 방식으로 바꿔 챕터/레슨 생성, 삭제, 순서 변경이 실제 저장 결과와 일치하게 했습니다.
- CORS 허용 메서드와 도메인 설정을 정리해 홈 빌더 초안 저장 요청이 동작하도록 수정했습니다.
- 어드민 사용자/구독 관리, 월간 IB 공개 상세, 질문 관리 관련 권한과 조회 흐름을 보완했습니다.
- 질문 생성/삭제, 튜터링 예약/취소, 강의 수강 등록과 플레이어 접근에서 구독 권한을 백엔드에서 실제로 차감·복구·검증하도록 정리했습니다.
- 로그인과 토큰 재발급 흐름을 `refresh token + sessionVersion` 검증으로 바꿔 다른 기기 재로그인 시 이전 세션이 재발급 없이 종료되도록 정리했습니다.
- 추천학교 점수대와 국가별 학교 목록을 JSON 설정으로 저장하고 `/board/calculator`에서 공개 조회할 수 있도록 정리했습니다.
- 합격 예측 계산기 설정 저장 시 `Group1~Group6` 그룹 규칙과 과목별 SL/HL 허용 정보를 함께 sanitize 하도록 확장했습니다.
- 인증 성공 시 사용자 `lastAccessAt`을 주기적으로 갱신하고, 관리자 사용량 화면에서 학생별 질문/튜터링/수강 진도 사용량을 집계할 수 있도록 정리했습니다.
- 템플릿 메일 발송 실패를 상위로 전달하고, `/api/mail`에서 제목/본문/수신자 검증 실패와 SMTP 실패를 의미 있는 에러 메시지로 반환하도록 정리했습니다.
# 2026-04-08
- Connect `Monthly IB` create/update and detail responses to the stored `content` field so the admin write screen can edit article text.
- Make admin finance OpenAI cost lookup fall back to `OPENAI_API_KEY` when `OPENAI_COSTS_API_KEY` is not configured.
- Use a dedicated finance `RestTemplate` with a longer read timeout so OpenAI cost aggregation does not fail on slower organization cost queries.
- Increase the finance OpenAI timeout again to cover paginated 12-month cost queries that can take close to a minute.
- Encode paginated OpenAI cost query URIs correctly so `next_page` tokens in long-range finance requests do not break follow-up calls.
