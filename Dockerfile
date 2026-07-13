# --- build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

COPY gradle gradle
COPY gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
COPY kyc-domain kyc-domain
COPY src src

# The wrapper jar is generated locally with `gradle wrapper`. If you're building
# in CI without it, install gradle and run `gradle wrapper --gradle-version 8.10.2` first.
RUN chmod +x gradlew && ./gradlew --no-daemon bootJar -x test

# --- runtime ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /src/build/libs/*.jar /app/app.jar

USER app

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -XX:MaxRAMPercentage=75"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
