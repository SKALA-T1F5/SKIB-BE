# 🧠 SKIB-BE(Spring Boot Backend)
AI 기반 시스템 매뉴얼/아키텍처 문서로부터 문항을 생성하고 피드백을 제공하는 지식 이전 플랫폼입니다.  

---

## 🚀 기술스택

| 구분 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.5.0 |
| **Security** | Spring Security + JWT 인증 |
| **API 문서화** | Swagger 3.0 (OpenAPI + JWT 인증 연동) |
| **Database** | - **MariaDB**: Spring Data JPA 기반 관계형 DB<br>- **MongoDB**: Spring Data MongoDB를 통한 NoSQL 저장 |
| **Query** | QueryDSL 5.0 |
| **Reactive / Web** | Spring WebFlux, WebSocket 일부 활용 |
| **환경 설정** | 다중 프로파일 (`application-local.yaml`, `application-prod.yaml` 등) |
| **환경변수 관리** | `.env` 파일 기반 환경 변수 주입 (`java-dotenv`) |
| **CI/CD** | - GitHub Actions (`.github/workflows/deploy.yaml`)<br>- Jenkins (`Jenkinsfile`)<br>- ArgoCD (`argocd/application.yaml`) |
| **Containerization** | Docker |
| **Orchestration** | Kubernetes (YAML 리소스: deploy, service, ingress) |
| **Cloud API** | Google Cloud Translation API 연동 (`google-cloud-translate`) |

---

## 📁 프로젝트 구조
SKIB-BE/
├── .github/                   # GitHub Actions 워크플로우 (CI/CD: deploy.yaml)
├── .vscode/                  # 개발 환경 설정 (settings.json)
├── argocd/                   # ArgoCD 배포 설정 (application.yaml)
├── gradle/                   # Gradle wrapper
├── k8s/                      # Kubernetes 배포 리소스 (deploy, service, ingress)
├── src/
│   └── main/
│       ├── java/com/t1f5/skib/
│       │   ├── auth/         # 로그인/인증(JWT) 모듈
│       │   ├── user/         # 사용자 정보 관리
│       │   ├── admin/        # 관리자 전용 기능
│       │   ├── project/      # 프로젝트 관리 (문서/트레이니/트레이너 연결)
│       │   ├── document/     # 문서 업로드 및 상태 관리
│       │   ├── question/     # 문제 생성, 검토, 저장
│       │   ├── test/         # 테스트(시험) 생성 및 응시
│       │   ├── answer/       # 답변 제출 및 채점
│       │   ├── feedback/     # 정오표, 피드백 분석
│       │   └── global/       # 전역 설정, 예외처리, Swagger, 공통 enum 등
│       └── resources/
│           ├── application.yaml
│           ├── application-local.yaml
│           └── application-prod.yaml
├── Dockerfile                # Spring Boot Docker 이미지 빌드 파일
├── Jenkinsfile              # Jenkins CI/CD 설정
├── build.gradle
├── settings.gradle
└── README.md

---

## Commit Code
1. `git add .`: 변경 사항 Staging
1. `git commit -m {commit_message}`: Commit Message 입력
1. `git push origin {branch}`: branch로 Push

## Commit Message
- `feat`: 새로운 기능 추가
- `fix`: 기능 수정, 버그 수정
- `refactor`: 코드 재구성
- `style`: 코드에 영향을 미치지 않는 수정(ex. 띄어쓰기, 이름, 주석 등)
- `docs`: 문서 추가 및 변경(ex. README.md, requirements.txt, .gitignore)
- `test`: 테스트코드, 리팩토링 테스트 코드 추가
- `chore`: 빌드 업무 수정, 패키지 매니저 수정
