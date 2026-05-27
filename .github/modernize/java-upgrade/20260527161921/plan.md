# Upgrade Plan: banking-backend (20260527161921)

- **Generated**: 27. Mai 2026, 18:30
- **HEAD Branch**: modernize/java-20260527181653
- **HEAD Commit ID**: 626056e16e4388c8912d376f90c4d8303707e9bd

---

## Available Tools

**JDKs**
- JDK 21.0.6: `/Users/philippe/Library/Java/JavaVirtualMachines/temurin-21.0.6/Contents/Home/bin` (current project JDK, used by Step 2 Baseline)
- JDK 25: **<TO_BE_INSTALLED>** (required by Steps 4 and 5)

**Build Tools**
- Maven 3.9.9: `/opt/homebrew/Cellar/maven/3.9.9/bin` (available via Homebrew/PATH; no Maven wrapper present)

> Note: Maven 3.9.9 is sufficient for Java 25. No wrapper upgrade required (no `mvnw` present).

---

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

---

## Options

- Working branch: modernize/java-20260527181653
- Run tests before and after the upgrade: true

---

## Upgrade Goals

- **Java**: 21 → **25** (latest LTS, released September 2025)
- **Spring Boot**: 3.5.6 → **4.0.6** (latest GA, released 2025/2026)

---

## Technology Stack

| Technology/Dependency              | Current           | Min Compatible | Why Incompatible                                                                      |
| ---------------------------------- | ----------------- | -------------- | ------------------------------------------------------------------------------------- |
| Java                               | 21                | 25             | User requested                                                                        |
| Spring Boot (BOM import)           | 3.5.6             | 4.0.6          | User requested; Spring Boot 4.0 is based on Spring Framework 7 + Jakarta EE 11       |
| Spring Framework                   | 6.2.x (via SB)    | 7.0.x          | Transitively required by Spring Boot 4.0                                              |
| JUnit BOM                          | 5.10.1 (explicit) | 6.1.0          | Spring Framework 7.0 sets JUnit 6 as minimum baseline                                |
| Cucumber BOM                       | 7.14.0 (explicit) | 7.34.3         | Align with latest Cucumber 7.x; Spring Boot 4.0 BOM does not manage Cucumber          |
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
