FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew gradlew.bat build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew \
    && printf "hilla.active=false\n" > ./gradle.properties

COPY src ./src

# Build Vaadin production bundle and runnable Spring Boot jar.
# Production mode prevents dev-server from trying to generate frontend resources at runtime.
RUN ./gradlew --no-daemon clean vaadinBuildFrontend bootJar -Pvaadin.productionMode -x test

## Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar ./app.jar

EXPOSE 8080

ENV JAVA_OPTS=""
ENV VAADIN_PRODUCTION_MODE="true"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

