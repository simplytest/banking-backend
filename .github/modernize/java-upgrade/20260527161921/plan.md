# Upgrade Plan: banking-backend (20260527161921)

- **Generated**: 2026-05-27 19:55:38
- **HEAD Branch**: modernize/java-20260527195304
- **HEAD Commit ID**: ea5e82de06f6f1807fc691708ad8dbcb6cb3269e

## Available Tools

**JDKs**
- JDK 25.0.2: /Users/philippe/.jdk/jdk-25.0.2/jdk-25.0.2+10/Contents/Home/bin (current project JDK, used by all steps)

**Build Tools**
- Maven 3.9.9: /opt/homebrew/Cellar/maven/3.9.9/bin (compatible with Java 25)

## Guidelines

> Your project is already configured at cutting-edge versions (Java 25, Spring Boot 4.0.6). This upgrade focuses on ensuring all dependencies are at their latest compatible patch versions and verifying full compilation and test success.

## Options

- Working branch: modernize/java-20260527195304
- Run tests before and after the upgrade: true

## Upgrade Goals

- **Java**: 25 (current: 25) — Keep latest
- **Spring Boot**: 4.0.6 (current: 4.0.6) — Ensure latest 4.0.x patch

## Technology Stack

| Technology/Dependency | Current | Latest Compatible | Status |
|---|---|---|---|
| Java | 25 | 25 | ✅ Current |
| Spring Boot | 4.0.6 | 4.0.6 | ✅ Current |
| Maven | 3.9.9 | 3.9.9 | ✅ Current |
| maven-compiler-plugin | 3.14.0 | 3.14.0 | ✅ Current |
| maven-surefire-plugin | 3.5.3 | 3.5.3 | ✅ Current |
| JUnit BOM | 6.1.0 | 6.1.0 | ✅ Current |
| Cucumber BOM | 7.34.3 | 7.34.3 | ✅ Current |
| springdoc-openapi-starter-webmvc-ui | 3.0.3 | 3.0.3 | ✅ Current |
| org.json | 20231013 | 20240303+ | ⚠️ Outdated |
| gson | 2.10.1 | 2.10.1 | ✅ Current |
| jjwt | 0.12.5 | 0.12.5 | ✅ Current |
| slf4j-api | 2.0.17 | 2.0.17+ | ⚠️ Check compatibility |
| httpclient5 | (managed by SB) | (managed by SB) | ✅ Current |
| Allure | 2.35.1 | 2.35.1 | ✅ Current |
| iban4j | 3.2.6-RELEASE | 3.2.6-RELEASE | ✅ Current |
| bcrypt | 0.10.2 | 0.10.2 | ✅ Current |
| wiremock-spring-boot | 4.2.1 | 4.2.1 | ✅ Current |
| h2 (test database) | (managed by SB) | (managed by SB) | ✅ Current |

## Derived Upgrades

1. **Ensure Java 25 runtime compatibility**: Maven must be configured to use Java 25.0.2 compiler; no breaking changes expected as Spring Boot 4.0.6 fully supports Java 25.
2. **Verify Docker image alignment**: Dockerfile already uses Java 25, ensure consistency during build.
3. **Dependency patch updates**: Update org.json and verify slf4j compatibility if newer versions available.

## Impact Analysis

### Subsection: Dependency Changes

No critical dependency changes required. The project is already at latest stable versions. Verification steps will ensure all dependencies compile and test correctly with Java 25.

| File | Dependency | Current | Action | Target | Reason |
|---|---|---|---|---|---|
| pom.xml (root) | maven.compiler.source/target | 25 | verify | 25 | Confirm Java 25 is set |
| BankingAppServer/pom.xml | spring.boot.version | 4.0.6 | verify | 4.0.6 | Latest Spring Boot 4.0.x |
| BankingAppCore/pom.xml | org.json (test scope) | 20231013 | upgrade | 20240303+ | Latest stable org.json |

### Subsection: Source Code Changes

No source code changes required. Spring Boot 4.0.6 is fully compatible with Java 25; no deprecated API usage detected in initial scan.

### Subsection: Configuration Changes

No configuration changes required. Current `application.properties`/`application.yml` settings are compatible with Spring Boot 4.0.6 and Java 25.

### Subsection: CI/CD Changes

| File | Location | Current | Required Change |
|---|---|---|---|
| Dockerfile | line 1 | eclipse-temurin:25-jdk-noble | No change (already using Java 25) |
| Dockerfile | lines 7-8 | Build command uses system Maven | No change (system Maven 3.9.9 is compatible) |

### Subsection: Risks & Warnings

- **Java 25 compiler requirement**: Maven MUST be configured to use Java 25.0.2 compiler. If system JAVA_HOME points to Java 23 or lower, compilation will fail with "Invalid target release: 25". **Mitigation**: Explicitly set JAVA_HOME to Java 25.0.2 during all build steps.
- **Module system behavior**: Java 25 has stronger encapsulation of internal APIs. The project uses standard APIs only; no JDK internal reflection detected, so no `--add-opens` workarounds needed.
- **Test coverage**: Existing test suite should fully validate Java 25 compatibility. No test-infrastructure breaking changes expected.

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: Ensure Java 25.0.2 is set as the active compiler; verify Maven 3.9.9 compatibility.
  - **Changes to Make**: Set JAVA_HOME to Java 25.0.2; verify Maven can compile with Java 25 target.
  - **Verification**: Command: `java -version && mvn --version`, JDK: Java 25.0.2, Expected Result: Both report Java 25 / Maven 3.9.9.

- **Step 2: Setup Baseline**
  - **Rationale**: Establish baseline compilation and test pass rate with Java 25 as control point for validation.
  - **Changes to Make**: Full compilation and test run on current state.
  - **Verification**: Command: `mvn clean test-compile -q && mvn clean test -q`, JDK: Java 25.0.2, Expected Result: Compilation SUCCESS, Tests: 100% pass.

- **Step 3: Verify Spring Boot 4.0.6 Compatibility & Dependencies**
  - **Rationale**: Confirm all Spring Boot 4.0.6 features and dependencies work as expected; update org.json to latest if newer version available.
  - **Changes to Make**: Run full test suite with Spring Boot 4.0.6 on Java 25; verify classpath.
  - **Verification**: Command: `mvn clean test -q`, JDK: Java 25.0.2, Expected Result: All tests pass; no deprecation warnings.

- **Step 4: Final Validation**
  - **Rationale**: Confirm all upgrade goals met, project is production-ready.
  - **Changes to Make**: Full integration validation, Docker build test, final compilation.
  - **Verification**: Command: `mvn clean verify && docker build -t banking-backend:latest .`, JDK: Java 25.0.2, Expected Result: BUILD SUCCESS, Docker image builds without errors.

---

**Notes**:
- The project is already on cutting-edge, stable versions. No breaking changes anticipated.
- All modules are set to Java 25 target and compile with `-parameters` flag (enables parameter name retention for reflection).
- Ensure JAVA_HOME is set correctly before each build step; Maven will not automatically use Java 25.
- CI/CD (Dockerfile) already uses correct Java 25 base image.
| springdoc-openapi-starter-webmvc-ui | 2.7.0             | 3.0.3          | springdoc 2.x supports Spring Boot 3.x only; Spring Boot 4.x requires springdoc 3.x  |
| wiremock-spring-boot               | 3.9.0             | 4.2.1          | wiremock-spring-boot 3.x targets Spring Boot 3.x; 4.x required for Spring Boot 4.0   |
| allure-junit5                      | 2.24.0 / 2.27.0   | 2.35.1         | Align with latest Allure 2.x; version inconsistency between modules                   |
| allure-cucumber7-jvm               | 2.24.0 / 2.27.0   | 2.35.1         | Align with allure-junit5 version                                                      |
| spring-boot-starter-web            | (used)            | -              | Renamed to `spring-boot-starter-webmvc` in Spring Boot 4.0 (deprecated alias exists)  |
| Dockerfile base image (Fedora 38)  | java-17-openjdk   | java-25        | Compiled bytecode targets Java 25; JVM must be ≥ 25 to run                            |
| maven-compiler-plugin              | 3.14.0            | 3.14.0         | Already compatible; no change needed                                                   |
| maven-surefire-plugin              | 3.5.3             | 3.5.3          | Already compatible; no change needed                                                   |

---

## Derived Upgrades

| Upgrade                                          | Reason                                                                                 |
| ------------------------------------------------ | -------------------------------------------------------------------------------------- |
| JUnit BOM 5.10.1 → 6.1.0                        | Spring Framework 7.0 requires JUnit 6 as minimum baseline                              |
| Cucumber BOM 7.14.0 → 7.34.3                    | JUnit 6 compatibility; align with latest stable Cucumber 7.x                           |
| allure 2.24/2.27 → 2.35.1 (both modules)        | JUnit 6 / Cucumber 7.34 compatibility; fix version inconsistency between modules       |
| springdoc-openapi 2.7.0 → 3.0.3                 | springdoc 2.x not compatible with Spring Boot 4.x                                      |
| wiremock-spring-boot 3.9.0 → 4.2.1              | wiremock-spring-boot 4.x targets Spring Boot 4.0                                       |
| spring-boot-starter-classic + test-classic added | Spring Boot 4.0 modularized: classic starters preserve all auto-configuration classpath |
| spring-boot-starter-webmvc replaces -web         | Canonical name in Spring Boot 4.0 (deprecated alias still works but should be updated) |
| spring-boot-resttestclient added (test scope)    | TestRestTemplate moved to new module in Spring Boot 4.0                                 |
| TestRestTemplate import updated in 7 test files  | Package moved: `o.s.b.test.web.client` → `o.s.b.resttestclient`                       |
| @AutoConfigureTestRestTemplate added to 7 tests  | @SpringBootTest no longer auto-configures TestRestTemplate in Spring Boot 4.0          |
| Java compiler source/target: 21 → 25            | User requested                                                                          |
| Dockerfile updated to Java 25 base image         | App compiled to Java 25 bytecode requires JVM ≥ 25                                     |

---

## Impact Analysis

### Dependency Changes

| File                          | Dependency                                | Current   | Action  | Target                  | Reason                                               |
| ----------------------------- | ----------------------------------------- | --------- | ------- | ----------------------- | ---------------------------------------------------- |
| pom.xml (root)                | maven.compiler.source/target              | 21        | upgrade | 25                      | User requested Java 25                               |
| BankingAppServer/pom.xml      | spring-boot-dependencies BOM              | 3.5.6     | upgrade | 4.0.6                   | User requested Spring Boot 4.0.6                     |
| BankingAppServer/pom.xml      | junit-bom                                 | 5.10.1    | upgrade | 6.1.0                   | Spring Framework 7.0 requires JUnit 6                |
| BankingAppServer/pom.xml      | cucumber-bom                              | 7.14.0    | upgrade | 7.34.3                  | JUnit 6 / Spring Boot 4.0 compatibility              |
| BankingAppServer/pom.xml      | spring-boot-starter-web                   | (used)    | replace | spring-boot-starter-webmvc | Renamed in SB 4.0                                 |
| BankingAppServer/pom.xml      | spring-boot-starter-classic               | (absent)  | add     | (managed by SB 4.0 BOM) | Ensures all auto-configurations available post-split |
| BankingAppServer/pom.xml      | spring-boot-starter-test-classic (test)   | (absent)  | add     | (managed by SB 4.0 BOM) | Ensures all test infrastructure available            |
| BankingAppServer/pom.xml      | spring-boot-resttestclient (test)         | (absent)  | add     | (managed by SB 4.0 BOM) | TestRestTemplate module in SB 4.0                    |
| BankingAppServer/pom.xml      | springdoc-openapi-starter-webmvc-ui       | 2.7.0     | upgrade | 3.0.3                   | springdoc 2.x incompatible with SB 4.x              |
| BankingAppServer/pom.xml      | wiremock-spring-boot                      | 3.9.0     | upgrade | 4.2.1                   | wiremock-spring-boot 4.x targets SB 4.0             |
| BankingAppServer/pom.xml      | allure-junit5                             | 2.24.0    | upgrade | 2.35.1                  | JUnit 6 / SB 4.0 compatibility; align versions      |
| BankingAppServer/pom.xml      | allure-cucumber7-jvm                      | 2.27.0    | upgrade | 2.35.1                  | Align with allure-junit5                             |
| BankingAppCore/pom.xml        | junit-bom                                 | 5.10.1    | upgrade | 6.1.0                   | Spring Framework 7.0 requires JUnit 6                |
| BankingAppCore/pom.xml        | cucumber-bom                              | 7.14.0    | upgrade | 7.34.3                  | JUnit 6 compatibility                                |
| BankingAppCore/pom.xml        | allure-junit5                             | 2.24.0    | upgrade | 2.35.1                  | JUnit 6 compatibility; align versions                |
| BankingAppCore/pom.xml        | allure-cucumber7-jvm                      | 2.24.0    | upgrade | 2.35.1                  | Align with allure-junit5                             |

### Source Code Changes

| File                                                   | Location             | Current                                                   | Required Change                                                                 | Reason                                                        |
| ------------------------------------------------------ | -------------------- | --------------------------------------------------------- | ------------------------------------------------------------------------------- | ------------------------------------------------------------- |
| `Aufgabe_4_2_1_loginAPI_Test.java`                     | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |
| `Aufgabe_4_2_1_loginAPI_Test.java`                     | class annotation     | `@SpringBootTest(...)`                                    | Add `@AutoConfigureTestRestTemplate` annotation                                  | SB 4.0 no longer auto-configures TestRestTemplate             |
| `Aufgabe_4_2_2_registerAPI_Test.java`                  | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |
| `Aufgabe_4_2_2_registerAPI_Test.java`                  | class annotation     | `@SpringBootTest(...)`                                    | Add `@AutoConfigureTestRestTemplate` annotation                                  | SB 4.0 no longer auto-configures TestRestTemplate             |
| `Aufgabe_4_2_3_retrieveAPI_Test.java`                  | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |
| `Aufgabe_4_2_3_retrieveAPI_Test.java`                  | class annotation     | `@SpringBootTest(...)`                                    | Add `@AutoConfigureTestRestTemplate` annotation                                  | SB 4.0 no longer auto-configures TestRestTemplate             |
| `Aufgabe_4_2_4_deleteAPI_Test.java`                    | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |
| `Aufgabe_4_2_4_deleteAPI_Test.java`                    | class annotation     | `@SpringBootTest(...)`                                    | Add `@AutoConfigureTestRestTemplate` annotation                                  | SB 4.0 no longer auto-configures TestRestTemplate             |
| `Aufgabe_5_1_retrieveAPI_DB_Injection_Test.java`       | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |
| `Aufgabe_5_1_retrieveAPI_DB_Injection_Test.java`       | class annotation     | `@SpringBootTest(...)`                                    | Add `@AutoConfigureTestRestTemplate` annotation                                  | SB 4.0 no longer auto-configures TestRestTemplate             |
| `Aufgabe_5_2_addAccount_DB_Injection_Test.java`        | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |
| `Aufgabe_5_2_addAccount_DB_Injection_Test.java`        | class annotation     | `@SpringBootTest(...)`                                    | Add `@AutoConfigureTestRestTemplate` annotation                                  | SB 4.0 no longer auto-configures TestRestTemplate             |
| `Aufgabe_5_3_retrieveBalanceAPI_DBMock_Test.java`      | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |
| `Aufgabe_5_3_retrieveBalanceAPI_DBMock_Test.java`      | class annotation     | `@SpringBootTest(...)`                                    | Add `@AutoConfigureTestRestTemplate` annotation                                  | SB 4.0 no longer auto-configures TestRestTemplate             |
| `Aufgabe_5_4_sendMoneyAPI_WireMock_Test.java`          | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |
| `Aufgabe_5_4_sendMoneyAPI_WireMock_Test.java`          | class annotation     | `@SpringBootTest(...)`                                    | Add `@AutoConfigureTestRestTemplate` annotation                                  | SB 4.0 no longer auto-configures TestRestTemplate             |
| `ContractUtils.java` (test util)                       | import               | `o.s.b.test.web.client.TestRestTemplate`                  | `org.springframework.boot.resttestclient.TestRestTemplate`                      | Package moved in Spring Boot 4.0                              |

### Configuration Changes

No changes required to `application.yml` or `application.properties` for this upgrade.
The `server.error.include-message: always` property is still valid in Spring Boot 4.0.

### CI/CD Changes

| File         | Location                   | Current                         | Required Change                                                                          |
| ------------ | -------------------------- | ------------------------------- | ---------------------------------------------------------------------------------------- |
| `Dockerfile` | line 1 (FROM)              | `fedora:38`                     | Replace with `eclipse-temurin:25-jdk-noble` (Ubuntu Noble + JDK 25)                     |
| `Dockerfile` | line 4 (RUN dnf install)   | `dnf install -y java-17-openjdk maven` | Replace with `apt-get install -y maven` (JDK already in base image)             |

### Risks & Warnings

- **`WebConfig.java` `configureMessageConverters` signature**: Spring Framework 7.0 introduced a new `configureMessageConverters(HttpMessageConverters.ServerBuilder)` overload. The old `configureMessageConverters(List<HttpMessageConverter<?>>)` is likely kept as a `default void` no-op, meaning the existing override may silently have no effect. **Mitigation**: Verify during execution that GSON serialization still works as expected (test responses contain JSON). If the old method is removed or becomes a no-op, rewrite `WebConfig.configureMessageConverters` to use the new `HttpMessageConverters.ServerBuilder` API. The project already excludes `spring-boot-starter-json` (Jackson), so GSON must remain the active converter.
- **`BankingServer.java` uses `MockHttpServletResponse` in production code**: The `spring-test` dependency is compile-scoped (no `<scope>test</scope>`) and used in the main entrypoint. Spring Boot 4.0 updates `spring-test` for Servlet 6.1, but the class should remain available. The `MockHttpServletResponse` is used as a dummy response sink in the demo initialization. **Mitigation**: Verify compilation succeeds; no code change expected.
- **JPA `@Lob String` with Hibernate ORM 7.x**: Spring Boot 4.0 upgrades to Hibernate ORM 7.1/7.2 (JPA 3.2). The `@Lob` annotation on `DBContract.contract` (String type) may have different JDBC type mapping in Hibernate 7.x vs 6.x. With H2 in-memory DB (tests), behavior should be consistent. **Mitigation**: Run tests and verify contract serialization/deserialization works correctly.
- **`SourceHttpMessageConverter` in `WebConfig.java`**: The Servlet 6.1 baseline in Spring Framework 7.0 may have deprecated/adjusted `SourceHttpMessageConverter`. It converts `javax.xml.transform.Source` objects which are unused by this REST API. **Mitigation**: If compilation fails, remove `converters.add(new SourceHttpMessageConverter<>())` from `WebConfig.java` — its absence will not affect the banking API.
- **JUnit 6 API changes**: JUnit 6 (6.1.0) introduced breaking changes from JUnit 5. The test files use JUnit Jupiter annotations (`@Test`, `@Autowired`, `@SpringBootTest`) which are expected to be compatible. Cucumber `cucumber-junit-platform-engine` works via JUnit Platform (version-agnostic). **Mitigation**: Use `spring-boot-starter-test-classic` to ensure the full test infrastructure is available; fix any compilation errors iteratively.

---

## Upgrade Steps

- Step 1: Setup Environment — Install JDK 25
  - **Rationale**: JDK 25 is not available locally; required for Java 25 compilation in Steps 4 and 5.
  - **Changes to Make**: Install JDK 25 via `#appmod-install-jdk`; verify availability via `#appmod-list-jdks`.
  - **Verification**: Command: `#appmod-list-jdks(version: "25")`; JDK: n/a; Expected Result: JDK 25 listed.

- Step 2: Setup Baseline — Compile and test with current stack (JDK 21 + Spring Boot 3.5.6)
  - **Rationale**: Establish baseline pass rate before any changes; forms the acceptance criteria for the final validation.
  - **Changes to Make**: No file changes; run existing project as-is with JDK 21.
  - **Verification**: Command: `mvn clean test -q`; JDK: temurin-21.0.6; Expected Result: document compile status and test pass rate.

- Step 3: Upgrade to Spring Boot 4.0.6 and fix all breaking changes
  - **Rationale**: All Spring Boot 4.0.6, Spring Framework 7.0, and JUnit 6 migration work. Grouped into a single step to ensure the project remains compilable after all interrelated changes.
  - **Changes to Make**:
    - All **Dependency Changes** from Impact Analysis (both pom.xml files)
    - All **Source Code Changes** from Impact Analysis (TestRestTemplate imports + `@AutoConfigureTestRestTemplate`)
    - **CI/CD Changes**: Update `Dockerfile` base image to `eclipse-temurin:25-jdk-noble` and install maven via apt
    - If the `configureMessageConverters` override is silently no-op (risk item): rewrite `WebConfig.java` with new `HttpMessageConverters.ServerBuilder` API
    - If `SourceHttpMessageConverter` causes compilation failure: remove it from `WebConfig.java`
  - **Verification**: Command: `mvn clean test-compile -q`; JDK: temurin-21.0.6; Expected Result: Compilation SUCCESS (main + test sources).

- Step 4: Upgrade Java compiler target to 25
  - **Rationale**: Upgrade compilation to Java 25 bytecode (see Dependency Changes in root `pom.xml`).
  - **Changes to Make**: Update `maven.compiler.source` and `maven.compiler.target` from `21` to `25` in root `pom.xml`.
  - **Verification**: Command: `mvn clean test-compile -q`; JDK: JDK 25 (installed in Step 1); Expected Result: Compilation SUCCESS with Java 25 bytecode.

- Step 5: Final Validation — Full test suite with JDK 25 + 100% pass rate
  - **Rationale**: Verify all upgrade goals are met; achieve ≥ baseline test pass rate; fix all failures.
  - **Changes to Make**: Iterative fix loop for any remaining test failures. Resolve all TODOs/workarounds.
  - **Verification**: Command: `mvn clean test -q`; JDK: JDK 25; Expected Result: All tests pass (≥ baseline pass rate). Java 25 + Spring Boot 4.0.6 confirmed.
