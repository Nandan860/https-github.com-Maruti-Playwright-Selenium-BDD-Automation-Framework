# =============================================================================
# Multi-stage Dockerfile
#
# Stage 1 (builder): Downloads dependencies, compiles, runs tests
# Stage 2 (report):  Minimal image with just the Allure report for serving
#
# Usage:
#   Build + run tests:
#     docker build --target builder -t qa-framework:latest .
#     docker run --rm \
#       -e browser=chrome -e headless=true \
#       -e app.url=https://your-app.com \
#       -v $(pwd)/target:/app/target \
#       qa-framework:latest
#
#   Serve Allure report:
#     docker build --target report -t qa-report:latest .
#     docker run -p 8080:8080 qa-report:latest
# =============================================================================

# ── Stage 1: Test execution ───────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

LABEL maintainer="qa-team"
LABEL description="Enterprise QA Automation Framework"

# Install system dependencies for Chrome headless
RUN apk add --no-cache \
    chromium \
    chromium-chromedriver \
    firefox \
    curl \
    bash \
    postgresql-client \
    font-noto \
    font-noto-emoji

WORKDIR /app

# Copy POM first — Docker layer cache: dependencies only re-downloaded when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress 2>/dev/null || \
    mvn dependency:go-offline -B

# Copy source
COPY src ./src
COPY sql ./sql
COPY docker ./docker

# Install Playwright browsers
RUN mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI \
    -D exec.args="install chromium" 2>/dev/null || true

# Default: run smoke suite
CMD mvn test \
    -DsuiteXmlFile=src/test/resources/config/testng-smoke.xml \
    -Dbrowser=${browser:-chrome} \
    -Dheadless=${headless:-true} \
    -Denv=${env:-dev} \
    -DCHROME_PATH=/usr/bin/chromium-browser \
    -DwebDriverManager.chromeDriverPath=/usr/bin/chromedriver \
    --no-transfer-progress

# ── Stage 2: Allure report server ─────────────────────────────────────────────
FROM openjdk:17-slim AS report

WORKDIR /report

RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/*

# Install Allure CLI
RUN curl -Lo allure.zip https://github.com/allure-framework/allure2/releases/download/2.25.0/allure-2.25.0.zip \
    && unzip allure.zip -d /opt \
    && ln -s /opt/allure-2.25.0/bin/allure /usr/local/bin/allure \
    && rm allure.zip

COPY --from=builder /app/target/allure-results ./allure-results

EXPOSE 8080
CMD ["allure", "serve", "allure-results", "--port", "8080", "--host", "0.0.0.0"]
