# Java 21용 경량 이미지
FROM eclipse-temurin:21-jdk

# JAR 파일 복사
COPY target/peoplejob-backend-0.0.1-SNAPSHOT.jar app.jar

# 포트 오픈
EXPOSE 8080

# 앱 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
