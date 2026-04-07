# Galaxy

전주대학교 캠퍼스 생활을 위한 Android 앱

## 주요 기능

- **대시보드** — 프로필, 학사일정 D-day, 도서관 좌석, 오늘의 학식, 최근 공지를 한 화면에
- **시간표** — 교시 기반 시간표 관리 (1~13교시)
- **학식** — 주간 식단표 조회 (조식/중식/석식)
- **도서관** — 실시간 좌석 현황, 인기 대출 도서
- **공지사항** — 카테고리별 공지 조회 (일반, 학사, 장학 등)
- **캠퍼스 지도** — 교내 건물 위치

## 기술 스택

| 영역 | 기술 |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Local DB | Room |
| Network | Retrofit + OkHttp + kotlinx.serialization |
| Image | Coil 3 |
| Navigation | Compose Navigation |

## 프로젝트 구조

```
app/src/main/java/com/example/galaxy/
├── data/
│   ├── local/          # Room DB, DAO, Entity
│   ├── location/       # 캠퍼스 위치 감지
│   ├── model/          # 도메인 모델
│   ├── remote/         # Retrofit 클라이언트, API 정의
│   ├── repository/     # Repository 패턴
│   └── source/         # 웹 스크래퍼 (학식, 공지)
├── ui/
│   ├── navigation/     # Route, NavGraph, BottomNavBar
│   ├── screen/         # 화면별 Screen + ViewModel
│   └── theme/          # Color, Theme, Typography
├── GalaxyApp.kt        # Application (Coil 초기화)
└── MainActivity.kt
```

## 빌드

1. Android Studio (Ladybug 이상) 에서 프로젝트 열기
2. Gradle Sync
3. Run (API 26+)

## 라이선스

학교 프로젝트용으로 제작되었습니다.
