# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# First, build and install the shared models library
COPY pizza-models/pom.xml ./pizza-models/
COPY pizza-models/src ./pizza-models/src
RUN cd pizza-models && mvn clean install -DskipTests

# Then build the payment service
COPY payment-service/pom.xml ./payment-service/
RUN cd payment-service && mvn dependency:go-offline -B
COPY payment-service/src ./payment-service/src
RUN cd payment-service && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/payment-service/target/payment-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
