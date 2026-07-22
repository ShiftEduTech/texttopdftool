FROM eclipse-temurin:17-jdk

WORKDIR /app

RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*

COPY . .

RUN chmod +x mvnw \
	&& ./mvnw clean package -DskipTests

EXPOSE 8080

ENV PORT=8080
ENV MAX_CONTENT_CHARS=100000
ENV RATE_LIMIT_RPM=60

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
	CMD curl -fsS "http://127.0.0.1:${PORT}/actuator/health" || exit 1

CMD ["sh", "-c", "java -jar target/texttopdftool-0.0.1-SNAPSHOT.jar"]
