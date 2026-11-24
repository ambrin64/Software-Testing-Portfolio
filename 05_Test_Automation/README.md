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
│   ├── web/                        WebServer, QuestionBank, Json (web front end)
│   └── Main.java                   console demo
├── src/main/resources/web/         ← web UI: index.html, styles.css, app.js
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
.\run_tests.ps1 -Web                              # start the web UI at http://localhost:8080/
.\run_tests.ps1 -Web -ExamSeconds 60              # web UI with a 1-minute exam (to show time-out)
python tools\junit_xml_to_html.py out\reports\TEST-junit-jupiter.xml report.html   # HTML report
```

Requires **JDK 17+**. Maven and Gradle are not needed.

## Web front end

`.\run_tests.ps1 -Web` starts a small web server (Java's built-in `com.sun.net.httpserver`, no extra
libraries) and opens the browser. Sign in with the demo account **demo@oes.edu / Exam2026** or register
a new student. Press `Ctrl+C` in the terminal to stop it.

The web layer (`com.oes.web`) contains **no business rules**. It only passes requests on to the same
classes that the 68 JUnit tests verify, so what you see in the browser is the tested behaviour:

| Screen | Behaviour shown | Class used | Related test cases |
|--------|-----------------|------------|--------------------|
| Register | Name / e-mail / age / password validation, duplicate e-mail (the returned status code is shown) | `RegistrationService` | TC_REG_EP_*, TC_REG_BVA_* |
| Sign in | Attempts left, lock after 3 failures, admin unlock | `AuthService` | TC_LGN_ST_* |
| Exam | Timer, change answer, answers locked after submit, automatic time-out | `ExamSession` | TC_EXM_ST_* |
| Result | +4 / −1 scoring, grade bands, PASS / FAIL / WITHHELD with the reason | `ResultCalculator` | TC_SCR_EP_*, TC_GRD_BVA_*, TC_RES_DT_* |

Leaving the exam window (switching tab or minimising) is recorded as an integrity warning. After 3
warnings the attempt is flagged as malpractice, so the result is **WITHHELD** whatever the marks
(decision table rules R7/R8, defect DEF-005). The correct answers are never sent to the browser.

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
