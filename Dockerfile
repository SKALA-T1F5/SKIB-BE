# ✅ OpenJDK 17 slim 버전을 베이스 이미지로 사용
FROM openjdk:17-jdk-slim

# ✅ 작업 디렉토리 설정
WORKDIR /app

# ✅ 외부 포트 오픈 (필요 시 추가 가능)
EXPOSE 8080
EXPOSE 8081

# ✅ JAR 파일을 고정 경로에서 컨테이너로 복사
COPY build/libs/skib-0.0.1-SNAPSHOT.jar app.jar

# ✅ 애플리케이션 실행 명령
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "app.jar"]