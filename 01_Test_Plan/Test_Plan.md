# Test Plan — Online Examination System (OES)

| Field | Value |
|-------|-------|
| Test Plan ID | TP-OES-01 |
| Version | 1.0 |
| Prepared by | Ambrin RK (1MS23IS011), Aryan Raj (1MS23IS022), BR Neha (1MS23IS025), Daksh Malik (1MS23IS036) |
| Standard followed | IEEE 829 (adapted) |

---

## 1. Introduction

The Online Examination System (OES) allows students to register, log in, attempt a timed
multiple-choice examination and view their score, grade and result. This plan describes the
scope, approach, resources, schedule and criteria for testing the core functional modules of OES.

## 2. Test Items

| Item | Source location |
|------|-----------------|
| Registration module | `05_Test_Automation/src/main/java/com/oes/registration` |
| Authentication module | `05_Test_Automation/src/main/java/com/oes/auth` |
| Exam session module | `05_Test_Automation/src/main/java/com/oes/exam` |
| Result module (scoring, grading, result status) | `05_Test_Automation/src/main/java/com/oes/result` |

## 3. Requirements / Features to be Tested

| Req. ID | Module | Requirement |
|---------|--------|-------------|
| REQ-01 | Registration | Name must be 3–30 characters long (after trimming spaces). |
| REQ-02 | Registration | E-mail must be in a valid format (`local@domain.tld`). |
| REQ-03 | Registration | An e-mail address can be registered only once. |
| REQ-04 | Registration | Age must be between **16 and 60 years (inclusive)**. |
| REQ-05 | Registration | Password must be 8–16 characters and contain at least one uppercase letter and one digit. |
| REQ-06 | Login | A registered user with correct credentials can log in. |
| REQ-07 | Login | After **3 consecutive** failed attempts the account is locked. |
| REQ-08 | Login | A successful login **resets** the failed-attempt counter to 0. |
| REQ-09 | Login | A locked account rejects even the correct password until an administrator unlocks it. |
| REQ-10 | Exam | An exam must be started before questions can be answered or the exam submitted. |
| REQ-11 | Exam | A student may change an answer while the exam is in progress; the last answer is saved. |
| REQ-12 | Exam | After submission **no further answers** are accepted. |
| REQ-13 | Exam | When the exam duration elapses the exam is automatically closed (TIMED_OUT). |
| REQ-14 | Scoring | +4 marks per correct answer, −1 per wrong answer, 0 for unanswered; total score never below 0. |
| REQ-15 | Grading | Grade bands: 90–100 → A+, 75–89 → A, 60–74 → B, 40–59 → C, 0–39 → F; outside 0–100 is invalid. |
| REQ-16 | Result | Malpractice → **WITHHELD** (regardless of marks). Otherwise PASS only if percentage ≥ 40 **and** all sections attempted; else FAIL. |

## 4. Features Not to be Tested

- User interface look & feel, browser compatibility and responsiveness.
- Performance, load and security (penetration) testing.
- Payment of examination fees, e-mail notifications and proctoring hardware.

## 5. Test Approach

| Level / Type | Description |
|--------------|-------------|
| Unit / component testing | Each module's business logic is tested in isolation. |
| Black-box test design | **Equivalence Partitioning** (Registration, Scoring), **Boundary Value Analysis** (Registration age / name / password length, Grade bands), **Decision Table** (Result status), **State Transition** (Login lock-out, Exam session life cycle). |
| Automation | All designed test cases are automated with **JUnit 5**; each automated test is named with its test-case ID for traceability. |
| Regression | After defects are fixed, the whole suite is re-executed (Cycle 2). |

## 6. Test Environment

| Component | Details |
|-----------|---------|
| OS | Windows 11 |
| JDK | Oracle JDK 21 |
| Test framework | JUnit 5 — junit-platform-console-standalone 1.11.4 |
| Version control | Git 2.x, GitHub |
| Report generation | Python 3 (JUnit XML → HTML) |

## 7. Entry and Exit Criteria

**Entry criteria**
- Requirements (Section 3) reviewed and baselined.
- Application build compiles without errors.
- Test cases designed, reviewed and automated.

**Exit criteria**
- 100 % of planned test cases executed.
- All Critical / High severity defects fixed and retested.
- Pass rate in the final cycle ≥ 95 %.
- Test execution summary report prepared.

## 8. Suspension and Resumption Criteria

- **Suspend** if the build fails to compile or more than 50 % of test cases are blocked.
- **Resume** once a new build that fixes the blocking issue is available.

## 9. Test Deliverables

| Deliverable | Location |
|-------------|----------|
| Test plan | `01_Test_Plan/` |
| Test case design & specification | `02_Test_Case_Design/` |
| Test execution logs | `03_Test_Execution/` |
| Defect reports | `04_Defect_Reports/` |
| Automation scripts | `05_Test_Automation/` |
| Test summary report | `06_Test_Reports/` |
| Screenshots | `07_Screenshots/` |

## 10. Roles and Responsibilities

| Role | Member | Responsibility |
|------|--------|----------------|
| Test Lead / Automation | Ambrin RK | Test plan, review, JUnit automation, GitHub repository maintenance |
| Test Designer | Aryan Raj | Test case design using EP, BVA, decision table, state transition |
| Test Executor | BR Neha | Test execution, recording actual results |
| Defect & Report Manager | Daksh Malik | Defect logging, tracking, retesting, summary report |

## 11. Schedule

| # | Activity | Duration |
|---|----------|----------|
| 1 | Requirement analysis & test plan | Week 1 |
| 2 | Test case design | Week 2 |
| 3 | Test automation (JUnit) | Week 3 |
| 4 | Test execution — Cycle 1, defect logging | Week 3 |
| 5 | Defect fixing, retest & regression — Cycle 2 | Week 4 |
| 6 | Summary report, documentation & demo | Week 4 |

## 12. Risks and Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Requirements ambiguous (e.g. boundary inclusive / exclusive) | Wrong expected results | Requirements written with explicit inclusive ranges (Section 3). |
| Time-dependent behaviour (exam timer) hard to test | Flaky tests | Clock is injected (`TimeSource`), so tests control time deterministically. |
| Limited time for manual execution | Incomplete coverage | All test cases automated with JUnit 5. |

## 13. Defect Severity Definitions

| Severity | Definition |
|----------|------------|
| Critical | Causes data corruption / wrong exam results for students; no workaround. |
| High | Major function works incorrectly; workaround difficult. |
| Medium | Function works incorrectly in specific conditions; workaround exists. |
| Low | Cosmetic or minor issue. |

## 14. Approvals

| Name | Role | Signature / Date |
|------|------|------------------|
| Ambrin RK | Test Lead | |
| | Faculty In-charge | |
