# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Build and install the shared models library first
COPY pizza-models/pom.xml ./pizza-models/
COPY pizza-models/src ./pizza-models/src
RUN cd pizza-models && mvn clean install -DskipTests

# Build the delivery service
COPY delivery-service/pom.xml ./delivery-service/
RUN cd delivery-service && mvn dependency:go-offline -B
COPY delivery-service/src ./delivery-service/src
RUN cd delivery-service && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/delivery-service/target/delivery-service-*.jar app.jar
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
