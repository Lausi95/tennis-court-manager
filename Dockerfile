FROM openjdk:21-rc-slim AS builder

WORKDIR /src
COPY . .
RUN ./gradlew build -x test

FROM openjdk:21-rc-slim

WORKDIR /application
EXPOSE 8080

COPY --from=builder /src/build/libs/application.jar ./application.jar

ENTRYPOINT ["sh", "-c"]
CMD ["java -jar application.jar"]