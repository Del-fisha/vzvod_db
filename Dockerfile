FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Docker / some networks break IPv6 to Maven repos; JMix lives on global.repo.jmix.io only.
ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"

COPY gradlew gradlew.bat build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew \
    && printf '%s\n' 'hilla.active=false' 'systemProp.java.net.preferIPv4Stack=true' > ./gradle.properties

COPY src ./src

# Build Vaadin production bundle and runnable Spring Boot jar.
# Production mode prevents dev-server from trying to generate frontend resources at runtime.
# If you see "global.repo.jmix.io:443 failed to respond": check host VPN/firewall and Docker DNS
# (Docker Desktop → Settings → Docker Engine: "dns": ["8.8.8.8","1.1.1.1"]).
RUN set -e; \
    max=8; \
    i=1; \
    while [ "$i" -le "$max" ]; do \
      echo "[core docker] build attempt $i/$max"; \
      ./gradlew --no-daemon --console=plain \
        -Dorg.gradle.internal.http.connectionTimeout=600000 \
        -Dorg.gradle.internal.http.socketTimeout=600000 \
        -Dorg.gradle.internal.repository.max.retries=20 \
        -Dorg.gradle.internal.repository.initial.backoff=3000 \
        clean vaadinBuildFrontend bootJar -Pvaadin.productionMode -x test \
        && break; \
      echo "[core docker] attempt $i failed, waiting 45s before retry..."; \
      i=$((i + 1)); \
      [ "$i" -le "$max" ] && sleep 45 || true; \
    done; \
    [ "$i" -le "$max" ] || { echo "[core docker] all attempts failed"; exit 1; }

## Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar ./app.jar

EXPOSE 8080

ENV JAVA_OPTS=""
ENV VAADIN_PRODUCTION_MODE="true"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

