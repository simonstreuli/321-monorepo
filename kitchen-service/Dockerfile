# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# First, build and install the shared models library
COPY pizza-models/pom.xml ./pizza-models/
COPY pizza-models/src ./pizza-models/src
RUN cd pizza-models && mvn clean install -DskipTests

# Then build the kitchen service
COPY kitchen-service/pom.xml ./kitchen-service/
RUN cd kitchen-service && mvn dependency:go-offline -B
COPY kitchen-service/src ./kitchen-service/src
RUN cd kitchen-service && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/kitchen-service/target/kitchen-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
