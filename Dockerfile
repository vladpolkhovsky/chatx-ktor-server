FROM amazoncorretto:17 AS builder

WORKDIR /app-server

COPY gradlew .
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY src src
COPY ktor-chat-web-server-dto ktor-chat-web-server-dto

RUN chmod 777 gradlew
RUN ./gradlew --no-daemon --build-cache clean buildFatJar

FROM amazoncorretto:17

WORKDIR /app

COPY --from=builder /app-server/build/libs/ktor-chat-webserver-all.jar .

ENV JWT_SECRET=SECRET
ENV JWT_DOMAIN=www.example.com
ENV USE_IN_MEMORY_DATABASE=true
ENV DB_URL=url
ENV DB_USER=user
ENV DB_PASSWORD=pass
ENV PORT=8080

EXPOSE ${PORT}

ENTRYPOINT ["java","-jar","ktor-chat-webserver-all.jar"]