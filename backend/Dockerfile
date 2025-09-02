FROM gradle:jdk-21-and-23-graal-jammy AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY src src
RUN gradle bootJar --no-daemon

FROM container-registry.oracle.com/graalvm/jdk:21 AS runner
WORKDIR /app

# 애플리케이션 사용자 생성
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 사용자 변경
USER appuser

# 포트 노출
EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]