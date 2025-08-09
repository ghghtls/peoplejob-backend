# Java 21용 경량 이미지
FROM eclipse-temurin:21-jdk

# 작업 디렉토리 설정
WORKDIR /app

# Maven wrapper와 pom.xml 복사
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# 의존성 다운로드 (캐시 효율성)
RUN ./mvnw dependency:go-offline

# 소스 코드 복사
COPY src/ src/

# 빌드 실행
RUN ./mvnw clean package -DskipTests

# 포트 오픈
EXPOSE 9000

# 앱 실행
ENTRYPOINT ["java", "-jar", "target/peoplejob-backend-0.0.1-SNAPSHOT.jar"]