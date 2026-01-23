# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# First, build and install the shared models library
COPY pizza-models/pom.xml ./pizza-models/
COPY pizza-models/src ./pizza-models/src
RUN cd pizza-models && mvn clean install -DskipTests

# Then build the order service
COPY order-service/pom.xml ./order-service/
RUN cd order-service && mvn dependency:go-offline -B
COPY order-service/src ./order-service/src
RUN cd order-service && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/order-service/target/order-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
