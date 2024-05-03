FROM gradle:jdk21 as builder

WORKDIR /src
COPY . .
RUN gradle build -x test

FROM openjdk:21-slim

WORKDIR /application
EXPOSE 8080

COPY --from=builder /src/build/libs/application.jar ./application.jar

ENTRYPOINT ["sh", "-c"]
CMD ["java -jar application.jar"]