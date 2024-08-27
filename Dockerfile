FROM amazoncorretto:18-alpine-full as builder
WORKDIR /app
COPY . /app/.
RUN ./mvnw -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM builder
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/*.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/*.jar"]


