# Java Upgrade Result

> **Executive Summary**\
> This report documents the successful upgrade of the banking-backend multi-module Maven project
> from Java 21 + Spring Boot 3.5.6 to **Java 25 + Spring Boot 4.0.6** (Spring Framework 7.0.7).
> The upgrade migrates the project to the very latest Java release and the newest Spring Boot
> major version, providing access to modern language features, updated security patches, and
> Jakarta EE 10 compatibility. All 20 tests pass at or above the pre-upgrade baseline (83.3%), with
> 4 pre-existing failures unchanged. No regressions were introduced by the upgrade.

## 1. Upgrade Improvements

Successfully upgraded from Java 21 / Spring Boot 3.5.6 to Java 25 / Spring Boot 4.0.6, modernising
the runtime, container image, and the full Spring ecosystem. The server module now uses the new
`spring-boot-starter-webmvc` starter and the new `TestRestTemplate` client from `spring-boot-resttestclient`.

| Area | Before | After | Improvement |
| ---- | ------ | ----- | ----------- |
| JDK | Java 21 | Java 25 | Latest Java release; new language features (e.g., Amber, Loom maturity) |
| Spring Boot | 3.5.6 | 4.0.6 | Latest major version; Jakarta EE 10, Spring Framework 7.0 |
| Spring Framework | 6.2.x | 7.0.7 | HTTP interface improvements, revised MVC defaults |
| JUnit | 5.10.1 (BOM) | 6.1.0 (BOM) | JUnit Jupiter 6 with improved extension model |
| Cucumber | 7.14.0 (BOM) | 7.34.3 (BOM) | Latest Cucumber with JUnit 6 support |
| Allure | 2.24.0–2.27.0 | 2.35.1 | JUnit 6 + Cucumber 7.34 compatibility |
| springdoc-openapi | 2.7.0 | 3.0.3 | Spring Boot 4.0 compatible |
| WireMock (Spring Boot) | 3.9.0 | 4.2.1 | Spring Boot 4.0 compatible |
| httpclient5 | (transitive) | 5.5.2 | Managed by Spring Boot 4.0 BOM |
| Dockerfile base image | fedora:38 + JDK 17 | eclipse-temurin:25-jdk-noble | Matches Java 25 runtime |

### Key Benefits

**Performance & Security**
- Java 25 provides the latest JVM performance improvements and security patches
- Spring Boot 4.0.6 incorporates all security fixes from the Spring ecosystem
- No CVEs detected in any direct dependency
- Migrated to `eclipse-temurin:25-jdk-noble` base image, eliminating outdated Fedora 38 + Java 17 container

**Developer Productivity**
- Access to latest Java language features (pattern matching, records improvements, structured concurrency preview)
- JUnit 6 and Cucumber 7.34.3 provide improved test extension model and BDD support
- Modernized test infrastructure: `TestRestTemplate` from new `spring-boot-resttestclient` module with `@AutoConfigureTestRestTemplate`
- springdoc-openapi 3.0.3 provides Spring Boot 4.0-compatible OpenAPI/Swagger UI

**Future-Ready Foundation**
- Aligned with latest Spring Boot major version for long-term support
- Jakarta EE 10 namespace fully adopted via Spring Framework 7.0
- Container image based on `eclipse-temurin` (Adoptium), the industry-standard OpenJDK distribution

## 2. Build and Validation

### Build Validation

| Field      | Value |
| ---------- | ----- |
| Status     | ✅ Success |
| Compiler   | Java 25.0.2 (JDK from jdk-25.0.2+10) |
| Build Tool | Maven 3.9.9 (`/opt/homebrew/Cellar/maven/3.9.9/bin/mvn`) |
| Result     | All source files (main + test) compiled successfully with no errors |

### Test Validation

| Field          | Value |
| -------------- | ----- |
| Status         | ✅ Success (≥ baseline) |
| Total Tests    | 24 |
| Passed         | 20 (83.3%) |
| Failed         | 4 (all pre-existing, unchanged from baseline) |
| Test Framework | JUnit Jupiter 6.1.0, Cucumber 7.34.3, Spring Boot Test |

**BankingAppServer (16/17 passed)**

| Test | Result | Notes |
| ---- | ------ | ----- |
| RegisterSpringTest | ✅ Passed | |
| Aufgabe_4_2_1_loginAPI_Test (4 tests) | ✅ Passed | |
| Aufgabe_4_2_2_registerAPI_Test (3 tests) | ✅ Passed | |
| Aufgabe_4_2_3_retrieveAPI_Test | ✅ Passed | |
| Aufgabe_4_2_4_deleteAPI_Test | ✅ Passed | |
| Aufgabe_5_1_retrieveAPI_DB_Injection_Test | ✅ Passed | |
| Aufgabe_5_2_addAccount_DB_Injection_Test (addImmoTilgungsKontoTest) | ❌ Failed | Pre-existing test bug — unchanged from baseline |
| Aufgabe_5_3_retrieveBalanceAPI_DBMock_Test (3 tests) | ✅ Passed | |
| Aufgabe_5_4_sendMoneyAPI_WireMock_Test (2 tests) | ✅ Passed | |
| CucumberIntegrationTest | ✅ Passed (0 scenarios) | BDD runner without active scenarios |

**BankingAppCore (4/7 passed)**

| Test | Result | Notes |
| ---- | ------ | ----- |
| Aufgabe_2_1_GiroAccountTests.testSendMoneyFails[1] | ✅ Passed | |
| Aufgabe_2_1_GiroAccountTests.testSendMoneyFails[2] | ❌ Failed | Pre-existing GiroAccount logic bug — unchanged from baseline |
| Aufgabe_2_1_GiroAccountTests.testSendMoneyFails[3] | ❌ Failed | Pre-existing GiroAccount logic bug — unchanged from baseline |
| Aufgabe_2_1_GiroAccountTests.testSendMoneyFails[4] | ❌ Failed | Pre-existing GiroAccount logic bug — unchanged from baseline |
| Aufgabe_2_1_GiroAccountTests.testSendMoneyFails[5] | ✅ Passed | |
| Aufgabe_3_JSON_Tests (2 tests) | ✅ Passed | |
| CucumberUnitTest | ✅ Passed (0 scenarios) | BDD runner without active scenarios |

---

## 3. Limitations

- **`configureMessageConverters(List<HttpMessageConverter<?>>)` deprecated in Spring Framework 7.0** (Acceptable)
  - `WebConfig.java` overrides the old `WebMvcConfigurer` method, which is deprecated in Spring Framework 7.0
  - Bytecode analysis of `WebMvcConfigurationSupport` confirms the old method is still called by `getMessageConverters()` — GSON converters are registered correctly and all API integration tests pass
  - Fix approach: migrate to the new `configureMessageConverters(HttpMessageConverters.ServerBuilder)` API
  - No breaking impact in Spring Boot 4.0.6; the method will be removed in a future Spring release

---

## 4. Recommended next steps

I. **Generate Unit Test Cases**: Instruction coverage is below 70% (BankingAppServer: 67%, BankingAppCore: 68%). Use the "Generate Unit Tests" agent to improve coverage, focusing on the `com.simplytest.server.api` (53%) and `com.simplytest.core.contracts` (50%) packages.

II. **Migrate `configureMessageConverters`**: Update `WebConfig.java` to use the new `HttpMessageConverters.ServerBuilder`-based API to resolve the Spring Framework 7.0 deprecation warning before it becomes a compile error in a future Spring release.

III. **Fix pre-existing test failures**: Investigate and fix the 4 pre-existing test failures:
   - `Aufgabe_2_1_GiroAccountTests.testSendMoneyFails[2,3,4]` — GiroAccount balance logic bug
   - `Aufgabe_5_2_addAccount_DB_Injection_Test.addImmoTilgungsKontoTest` — test data/assertion issue

IV. **Adopt modern Java 25 features**: Refactor to use records, pattern matching, text blocks, and sealed classes where appropriate to leverage the new Java version.

V. **Update CI/CD pipelines**: Verify all CI/CD environments and deployment scripts use JDK 25 and the updated `eclipse-temurin:25-jdk-noble` base image.

---

## 5. Additional details

<details>
<summary>Click to expand for upgrade details</summary>

### Project Details

| Field                 | Value                            |
| --------------------- | -------------------------------- |
| Session ID            | 20260527161921                   |
| Upgrade executed by   | philippe                         |
| Upgrade performed by  | GitHub Copilot                   |
| Project path          | /Users/philippe/Documents/Projects/banking-backend |
| Repository            | simplytest/banking-backend       |
| Build tool (before)   | Maven 3.9.9                      |
| Build tool (after)    | Maven 3.9.9 (unchanged)          |
| Files modified        | 16                               |
| Lines added / removed | +58 / -29                        |
| Branch created        | modernize/java-20260527181653    |

### Code Changes

1. **`pom.xml`** (root)
   - `maven.compiler.source/target`: 21 → 25

2. **`BankingAppServer/pom.xml`**
   - Spring Boot BOM: 3.5.6 → 4.0.6
   - JUnit BOM: 5.10.1 → 6.1.0; Cucumber BOM: 7.14.0 → 7.34.3
   - `spring-boot-starter-web` → `spring-boot-starter-webmvc`
   - Added: `spring-boot-starter-classic`, `spring-boot-starter-test-classic`, `spring-boot-resttestclient`
   - `springdoc-openapi-starter-webmvc-ui`: 2.7.0 → 3.0.3
   - `wiremock-spring-boot`: 3.9.0 → 4.2.1
   - `allure-junit5`, `allure-cucumber7-jvm`: 2.24.0/2.27.0 → 2.35.1
   - H2: explicit version pin removed (now managed by Spring Boot 4.0 BOM)

3. **`BankingAppCore/pom.xml`**
   - JUnit BOM: 5.10.1 → 6.1.0; Cucumber BOM: 7.14.0 → 7.34.3
   - `allure-junit5`, `allure-cucumber7-jvm`: 2.24.0 → 2.35.1

4. **`Dockerfile`**
   - Base image: `fedora:38` + `dnf install java-17-openjdk maven` → `eclipse-temurin:25-jdk-noble` + `apt-get install maven`

5. **`BankingAppServer/src/main/java/com/simplytest/server/utils/APIUtil.java`**
   - Import: `org.springframework.boot.web.context.WebServerInitializedEvent` → `org.springframework.boot.web.server.context.WebServerInitializedEvent` (package moved in Spring Boot 4.0)

6. **`BankingAppServer/src/main/java/com/simplytest/server/api/ErrorHandler.java`**
   - `new ResponseEntity<>(error, null, HttpStatus.BAD_REQUEST)` → `ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)` (ambiguous constructor in Spring Framework 7.0)

7. **`BankingAppServer/src/test/java/com/simplytest/server/example/RegisterSpringTest.java`**
   - `AutoConfigureMockMvc` import: `org.springframework.boot.test.autoconfigure.web.servlet` → `org.springframework.boot.webmvc.test.autoconfigure`

8. **9 integration test files** (`contractAPI/`, `accountAPI/`)
   - `TestRestTemplate` import: `org.springframework.boot.test.web.client` → `org.springframework.boot.resttestclient`
   - Added `@AutoConfigureTestRestTemplate` annotation and corresponding import to 8 test classes

### Automated tasks

- Dependency version upgrades (pom.xml files)
- Package relocation fixes for Spring Boot 4.0 API changes
- Container base image modernisation (Dockerfile)
- Import updates for relocated test annotations

### Potential Issues

#### CVEs

**Scan Status**: ✅ No known CVE vulnerabilities detected

**Scanned**: 24 direct dependencies | **Vulnerabilities Found**: 0

</details>
