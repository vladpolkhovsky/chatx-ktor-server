FROM amazoncorretto:22

RUN mkdir "/app-server"

COPY . /app-server
WORKDIR /app-server

RUN chmod 777 gradlew
RUN ./gradlew clean buildFatJar

WORKDIR /app-server/build/libs

ENV JWT_SECRET=SECRET
ENV JWT_DOMAIN=www.example.com
ENV USE_IN_MEMORY_DATABASE=true
ENV DB_URL=${KTOR_DB_URL}
ENV DB_USER=${KTOR_DB_USER}
ENV DB_PASSWORD=${KTOR_DB_PASSWORD}
ENV PORT=8080

EXPOSE ${PORT}

ENTRYPOINT ["java","-jar","ktor-chat-webserver-all.jar"]