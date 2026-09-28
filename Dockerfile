# Runtime image for PetHotelGo.
#
# The jar is built OUTSIDE Docker (locally or in CI) and copied in — compiling
# Kotlin + Spring inside the image needs ~1 GB RAM for the Kotlin daemon alone,
# which the target (OCI Always Free, 1 GB) cannot spare. Build first:
#
#   ./mvnw clean package -DskipTests
#
# then `docker compose ... up -d --build` (the build step is now just a COPY).
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

ARG JAR_FILE=target/pethotelgo-*.jar
COPY ${JAR_FILE} /app/app.jar

ENV JAVA_OPTS="-Xms64m -Xmx384m"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} \
  -jar /app/app.jar"]
