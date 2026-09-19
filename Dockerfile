FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]