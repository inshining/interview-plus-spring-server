# 1. Gradle을 사용하여 Spring Boot application 빌드
FROM gradle:7.6-jdk-alpine AS build
WORKDIR /app
COPY . .
#1-1. Gradle Wrapper 스크립트에 실행 권한을 부여
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 2. 빌드된 JAR 파일을 실행할 amazoncorretto:17 이미지로 이동
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/build/libs/resume-0.0.1-SNAPSHOT.jar app.jar


# 3. Spring Boot 애플리케이션 실행을 위해 Docker 내부에서 포트 8080을 노출
EXPOSE 8080

# 4. Docker 컨테이너 시작 시 Spring Boot 애플리케이션 실행
CMD ["java", "-jar", "app.jar","--server.address=0.0.0.0"]