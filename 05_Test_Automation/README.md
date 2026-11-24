# 05 — Test Automation (JUnit 5)

This folder contains the **application under test** (the Online Examination System's core logic)
and the **JUnit 5 automated test suite** that executes all 68 designed test cases.

## Folder layout

```
05_Test_Automation/
├── src/main/java/com/oes/          ← application under test
│   ├── registration/               RegistrationService, RegistrationStatus
│   ├── auth/                       AuthService, LoginResult
│   ├── exam/                       ExamSession, ExamState, Question, TimeSource
│   ├── result/                     ResultCalculator, ResultStatus
│   └── Main.java                   console demo
├── src/test/java/com/oes/          ← JUnit 5 test suite (68 tests)
│   ├── registration/RegistrationEquivalencePartitioningTest.java   (8)
│   ├── registration/RegistrationBoundaryValueTest.java             (14)
│   ├── auth/LoginStateTransitionTest.java                          (8)
│   ├── exam/ExamSessionStateTransitionTest.java                    (12)
│   ├── result/ScoreEquivalencePartitioningTest.java                (6)
│   ├── result/GradeBoundaryValueTest.java                          (12)
│   └── result/ResultDecisionTableTest.java                         (8)
├── lib/junit-platform-console-standalone-1.11.4.jar
├── tools/junit_xml_to_html.py      ← converts JUnit XML into HTML + Markdown reports
├── run_tests.ps1                   ← Windows runner
└── run_tests.sh                    ← Linux / macOS / Git Bash runner
```

## Running

```powershell
.\run_tests.ps1                                   # compile + run all tests
.\run_tests.ps1 -Demo                             # run the console demo
python tools\junit_xml_to_html.py out\reports\TEST-junit-jupiter.xml report.html   # HTML report
```

Requires **JDK 17+**. Maven and Gradle are not needed.

## JUnit 5 features used

| Feature | Where | Why |
|---------|-------|-----|
| `@DisplayName` with test case ID | every test | Traceability from design document to script to result |
| `@ParameterizedTest` + `@CsvSource` | BVA and decision table tests | One method runs a whole table of designed test cases |
| `@BeforeEach` | EP and state transition tests | Fresh, independent test fixture for every test |
| `assertThrows` | invalid state transitions, out-of-range grades | Verifies the required exception |
| `assertAll` | login counter tests | Reports *every* failed check, not only the first |
| Injected `TimeSource` (fake clock) | exam time-out tests | Tests time-outs instantly instead of waiting 30 minutes |

## Example: a decision table as a parameterized test

```java
@ParameterizedTest(name = "{0} ({1}): {2} %, malpractice={3}, allSections={4} -> {5}")
@CsvSource({
        "TC_RES_DT_01, R1, 65, false, true,  PASS",
        "TC_RES_DT_07, R7, 30, true,  true,  WITHHELD",
        ...
})
void decisionTable(String tcId, String rule, double percentage, boolean malpractice,
                   boolean allSections, ResultStatus expected) {
    assertEquals(expected, calc.status(percentage, malpractice, allSections));
}
```
