# 🏦 Enterprise QA Automation Framework
### Production-grade, dual-engine test automation covering UI · API · Database — built for scale, CI/CD-ready out of the box.

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java" />
  <img src="https://img.shields.io/badge/Selenium-4.x-43B02A?style=for-the-badge&logo=selenium" />
  <img src="https://img.shields.io/badge/Playwright-1.42-2EAD33?style=for-the-badge&logo=playwright" />
  <img src="https://img.shields.io/badge/Cucumber-7.x-23D96C?style=for-the-badge&logo=cucumber" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql" />
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker" />
  <img src="https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=for-the-badge&logo=githubactions" />
  <img src="https://img.shields.io/badge/Allure-Reports-orange?style=for-the-badge" />
</p>

---

## 📌 What This Framework Does & The Problem It Solves

Modern enterprise applications span multiple layers — a UI that must work flawlessly across browsers and operating systems, REST APIs that need contract and status validation, and databases that must stay consistent through migrations and ETL pipelines. Most frameworks test only one of these layers in isolation.

**This framework tests all three — simultaneously, reliably, and at scale.**

| Problem | Solution Delivered |
|---|---|
| Tests break when browser versions update | Dual-engine design (Selenium + Playwright) with WebDriverManager auto-resolution |
| Flaky tests pollute CI/CD pipelines | Configurable RetryAnalyzer auto-retries failed tests up to N times |
| No visibility into test failures | Allure Reports with screenshots, step-level detail, and history trends |
| ETL/migration data cannot be verified at scale | HikariCP-pooled PostgreSQL client with row-by-row cross-database comparison |
| Hard to run across Chrome, Firefox, Edge, WebKit | Single config switch; full browser matrix in GitHub Actions |
| New team members break the framework | BDD feature files readable by non-engineers; step defs follow single-responsibility |

---

## 🧰 Technologies Used

| Category | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Build Tool | Apache Maven | 3.9+ |
| UI Automation | Selenium WebDriver | 4.18 |
| UI Automation (Alt) | Microsoft Playwright | 1.42 |
| Driver Management | WebDriverManager | 5.7 |
| API Testing | REST Assured | 5.4 |
| BDD Layer | Cucumber | 7.15 |
| Test Runner | TestNG | 7.9 |
| Database | PostgreSQL | 16 |
| Connection Pool | HikariCP | 5.1 |
| Test Data | Apache POI (Excel) | 5.2 |
| Reporting | Allure Reports | 2.25 |
| Logging | Log4j2 | 2.23 |
| Assertions | AssertJ | 3.25 |
| Containerisation | Docker + Docker Compose | — |
| CI/CD | GitHub Actions | — |

---

## ✨ Key Features & Design Patterns

### 🔁 Dual Automation Engine
- **Selenium WebDriver** for Chrome, Firefox, and Edge
- **Microsoft Playwright** for Chromium, WebKit (Safari), and Firefox
- Single `engine=selenium|playwright` config switch — no code changes required
- Thread-local driver management ensures zero cross-test contamination in parallel runs

### 📐 Architecture & Design Patterns
- **Page Object Model (POM)** — every screen is a class; zero raw locators in step definitions
- **BDD (Behaviour-Driven Development)** — Gherkin feature files act as living documentation
- **Factory Pattern** — `DriverFactory` abstracts engine creation from test logic
- **Singleton Pattern** — `ConfigManager` and `DBClient` pools initialised once per JVM
- **Fluent API** — page methods return `this` for readable chained interactions
- **Thread-local Storage** — parallel-safe driver and page instances per thread

### 🌐 Cross-Browser & Cross-Platform
- Chrome · Firefox · Edge · WebKit · Chromium
- Windows · macOS · Linux (native + containerised)
- Remote Selenium Grid support via `grid.url` config property
- Full browser matrix in GitHub Actions CI pipeline

### 🔌 API Testing
- REST Assured wrapper with configurable base URI, headers, and auth
- Status code, response body, JSON path, and response header assertions
- File upload (multipart) and binary file download via API
- **API ↔ UI cross-validation** — verify the same data rendered in the browser matches the API response

### 🗄️ Database & ETL Validation
- Dual `DBClient` instances (source + target) for migration/ETL scenarios
- `compareWith()` method performs column-level row diff between two PostgreSQL databases
- HikariCP connection pooling — handles high-concurrency DB validation without exhausting connections
- Scalar queries, parameterised SQL, and DML execution helpers

### 📊 Data-Driven Testing
- Excel (`.xlsx`) reader via Apache POI — no test data hardcoded in step definitions
- `Scenario Outline` + Examples tables in Gherkin for tabular test data
- `ExcelReader.asDataProvider()` bridges directly to TestNG `@DataProvider`

### 🔗 Broken Link & Image Detection
- Concurrent HTTP HEAD checks across all `<a href>` and `<img src>` elements
- 10-thread pool for fast scanning of large pages
- Reports URL + HTTP status code for every broken resource

### 📁 File Upload & Download
- Selenium `sendKeys()` upload (headless-compatible, no AutoIT required)
- Playwright native download event handling
- API multipart file upload via REST Assured
- Download directory verification step

### 🔄 Retry & Stability
- `RetryAnalyzer` + `RetryListener` auto-applied to every test — no `@Test` annotation changes needed
- Max retry count configurable via `max.retry.count` property
- Screenshot captured on every failure and attached to Allure report

### 📈 Reporting
- **Allure Reports** with step-level pass/fail, screenshots on failure, and trend history
- Log4j2 structured logging to console and rolling file appender
- Test execution summary printed to CI pipeline output

---

## ✅ Prerequisites

Ensure the following are installed before running the framework:

| Tool | Minimum Version | Check Command |
|---|---|---|
| Java JDK | 17 | `java -version` |
| Apache Maven | 3.9 | `mvn -version` |
| Docker | 24+ | `docker -version` |
| PostgreSQL | 16 (or via Docker) | `psql --version` |
| Git | 2.x | `git --version` |

> **Note:** Browsers (Chrome, Firefox, Edge) are auto-managed by WebDriverManager. You do **not** need to manually install browser drivers.

---

## 🚀 Setup & Execution Guide

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/qa-automation-framework.git
cd qa-automation-framework
```

### 2. Configure Environment

Copy the dev config template and update with your values:

```bash
cp src/test/resources/config/dev.properties.template src/test/resources/config/dev.properties
```

Edit `dev.properties`:

```properties
# Application
app.url=https://your-app-url.com
api.base.url=https://your-api-url.com

# Browser
browser=chrome          # chrome | firefox | edge
engine=selenium         # selenium | playwright
headless=false

# PostgreSQL — Source DB
source.db.url=jdbc:postgresql://localhost:5432/legacy_db
source.db.user=postgres
source.db.password=secret

# PostgreSQL — Target DB
target.db.url=jdbc:postgresql://localhost:5432/new_db
target.db.user=postgres
target.db.password=secret

# Framework
explicit.wait=10
max.retry.count=2
download.dir=/tmp/downloads
```

### 3. Start Databases (Docker)

```bash
docker-compose up -d postgres-source postgres-target
```

### 4. Run Tests

**All tests (default suite):**
```bash
mvn clean test
```

**Specific browser:**
```bash
mvn clean test -Pbrowser=firefox
mvn clean test -Pbrowser=edge
```

**API tests only:**
```bash
mvn clean test -Papi
```

**ETL / DB validation only:**
```bash
mvn clean test -Pdb
```

**Playwright engine:**
```bash
mvn clean test -Dengine=playwright -Dbrowser=webkit
```

**Headless (CI mode):**
```bash
mvn clean test -Dheadless=true
```

**Parallel execution:**
```bash
mvn clean test -DthreadCount=4
```

### 5. Run in Docker

```bash
# Build image
docker build -t qa-framework .

# Run tests inside container
docker run --rm \
  -e browser=chrome \
  -e headless=true \
  -v $(pwd)/target:/app/target \
  qa-framework
```

---

## 📊 Test Reporting

### Allure Report (Recommended)

```bash
# Generate and open report
mvn allure:serve

# Generate static report only
mvn allure:report
# Report location: target/site/allure-maven-plugin/index.html
```

Allure provides:
- Step-by-step pass/fail breakdown per scenario
- Attached screenshots on failure
- Historical trend graphs across CI runs
- Categorised failures (product bugs vs infrastructure issues)

### Log Output

Logs are written to:
- **Console** — INFO level during execution
- **`target/logs/framework.log`** — DEBUG level rolling file

---

## 🗺️ Roadmap & Future Enhancements

| Priority | Enhancement |
|---|---|
| 🔴 High | Appium integration for Android/iOS mobile testing |
| 🔴 High | Selenium Grid 4 / Kubernetes node scaling |
| 🟡 Medium | Visual regression testing (Playwright snapshots) |
| 🟡 Medium | Contract testing with Pact |
| 🟡 Medium | Slack/Teams notifications on pipeline failure |
| 🟢 Low | AI-assisted self-healing locators |
| 🟢 Low | Performance baseline testing with Gatling |
| 🟢 Low | TestRail / Jira Xray integration for test case sync |

---

## 📁 Project Structure

```
qa-automation-framework/
├── src/
│   ├── main/java/com/framework/
│   │   ├── config/          # ConfigManager — central config loader
│   │   ├── driver/          # DriverFactory + BasePage
│   │   ├── api/             # ApiClient (REST Assured wrapper)
│   │   ├── db/              # DBClient (PostgreSQL + HikariCP)
│   │   └── utils/           # RetryAnalyzer, ExcelReader, BrokenLinkChecker, ScreenshotUtil
│   └── test/
│       ├── java/com/framework/
│       │   ├── pages/       # Page Object classes
│       │   ├── steps/       # Cucumber step definitions + Hooks
│       │   ├── tests/       # TestNG test classes (non-BDD)
│       │   └── runners/     # Cucumber runners per suite
│       └── resources/
│           ├── features/    # Gherkin .feature files
│           ├── testdata/    # Excel data files
│           └── config/      # Environment .properties files + testng.xml
├── docker/
├── sql/                     # PostgreSQL init scripts
├── .github/workflows/       # GitHub Actions CI pipeline
├── docker-compose.yml
└── pom.xml
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Follow the [Best Practices Wiki](../../wiki/Best-Practices) before committing
4. Submit a pull request with a clear description of what and why

---



---

<p align="center">Built with ☕ Java · Designed for scale · Ready for production</p>
