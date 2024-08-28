FROM amazoncorretto:18-alpine-full as builder
WORKDIR /app
COPY . /app/.
# clean up the file
RUN sed -i 's/\r$//' mvnw
RUN /bin/sh ./mvnw -f /app/pom.xml clean package -DskipTests

FROM builder
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/*.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/*.jar"]
