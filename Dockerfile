# Java 21용 경량 이미지
FROM eclipse-temurin:21-jdk

# ==================== CI/CD 메타데이터 추가 ====================
LABEL maintainer="peoplejob-team" \
      version="1.0" \
      description="PeopleJob Backend Application"

# ==================== 시스템 패키지 설치 (헬스체크용) ====================
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# Maven wrapper와 pom.xml 복사
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# ==================== Maven wrapper 실행 권한 부여 ====================
RUN chmod +x ./mvnw

# 의존성 다운로드 (캐시 효율성)
RUN ./mvnw dependency:go-offline

# 소스 코드 복사
COPY src/ src/

# 빌드 실행
RUN ./mvnw clean package -DskipTests

# 포트 오픈
EXPOSE 9000

# ==================== 헬스체크 추가 ====================
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9000/actuator/health || exit 1

# ==================== JVM 최적화 환경변수 추가 ====================
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 앱 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/peoplejob-backend-0.0.1-SNAPSHOT.jar"]