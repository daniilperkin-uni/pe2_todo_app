FROM maven:3-eclipse-temurin-25-alpine AS build-stage
WORKDIR /app
COPY api/pom.xml ./pom.xml
RUN mvn dependency:go-offline
COPY api/src ./src
RUN mvn clean install -DskipTests=true -Dcheckstyle.skip=true
RUN cp target/rest-api.jar ./app.jar

FROM eclipse-temurin:25-jre-alpine AS production-stage
WORKDIR /app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
COPY --from=build-stage /app/app.jar ./app.jar
ENTRYPOINT ["java","-jar","app.jar"]
