# Test Execution Summary Report — Online Examination System

| Field | Value |
|-------|-------|
| Report ID | TSR-OES-01 |
| Project | Online Examination System (OES) |
| Test plan reference | [TP-OES-01](../01_Test_Plan/Test_Plan.md) |
| Test period | Week 1 – Week 4 |
| Final build tested | Commit `7333127` |

## 1. Summary

All 68 designed test cases were automated with JUnit 5 and run in two cycles. Cycle 1 found
**5 defects** (7 failing test cases). The defects were fixed, and Cycle 2 **retested** them and ran
a full **regression** of the suite. **All 68 test cases passed** and every defect is closed, so
the exit criteria in the test plan are met.

## 2. Execution results by cycle

| Metric | Cycle 1 | Cycle 2 (retest + regression) |
|--------|:-------:|:-----------------------------:|
| Test cases planned | 68 | 68 |
| Executed | 68 | 68 |
| Passed | 61 | **68** |
| Failed | 7 | **0** |
| Blocked / skipped | 0 | 0 |
| Pass rate | 89.7 % | **100 %** |
| Execution time | ≈ 0.5 s | ≈ 0.5 s |
| Detailed log | [Cycle 1 log](../03_Test_Execution/Test_Execution_Log_Cycle1.md) | [Cycle 2 log](../03_Test_Execution/Test_Execution_Log_Cycle2.md) |
| HTML report | [Cycle1_Report.html](../03_Test_Execution/Cycle1_Results/Cycle1_Report.html) | [Cycle2_Report.html](../03_Test_Execution/Cycle2_Results/Cycle2_Report.html) |

## 3. Results by module and technique

| Module | Technique | Test cases | Cycle 1 pass / fail | Cycle 2 pass / fail | Defects found |
|--------|-----------|:---------:|:-------------------:|:-------------------:|:-------------:|
| Registration | Equivalence Partitioning | 8 | 8 / 0 | 8 / 0 | — |
| Registration | Boundary Value Analysis | 14 | 13 / 1 | 14 / 0 | DEF-001 |
| Login | State Transition | 8 | 6 / 2 | 8 / 0 | DEF-002 |
| Exam Session | State Transition | 12 | 11 / 1 | 12 / 0 | DEF-003 |
| Score Calculation | Equivalence Partitioning | 6 | 6 / 0 | 6 / 0 | — |
| Grading | Boundary Value Analysis | 12 | 11 / 1 | 12 / 0 | DEF-004 |
| Result Status | Decision Table | 8 | 6 / 2 | 8 / 0 | DEF-005 |
| **Total** | | **68** | **61 / 7** | **68 / 0** | **5** |

## 4. Defect summary

| Defect | Title | Severity | Status |
|--------|-------|----------|--------|
| [DEF-001](../04_Defect_Reports/DEF-001_Age_Upper_Boundary.md) | Student aged exactly 60 cannot register | Medium | Closed |
| [DEF-002](../04_Defect_Reports/DEF-002_Login_Counter_Not_Reset.md) | Failed-login counter not reset after successful login | High | Closed |
| [DEF-003](../04_Defect_Reports/DEF-003_Answer_After_Submit.md) | Answers can be changed after exam submission | Critical | Closed |
| [DEF-004](../04_Defect_Reports/DEF-004_Grade_Boundary_75.md) | 75 % graded B instead of A | High | Closed |
| [DEF-005](../04_Defect_Reports/DEF-005_Malpractice_Result_Status.md) | Malpractice below 40 % shows FAIL instead of WITHHELD | High | Closed |

- **Defects found:** 5 (Critical 1, High 3, Medium 1)
- **Defects fixed and closed:** 5 (100 %)
- **Open defects:** 0
- **Defect density:** 5 defects / 68 test cases ≈ 0.07 per test case

## 5. Exit criteria evaluation

| Exit criterion (Test Plan §7) | Result | Met? |
|-------------------------------|--------|:----:|
| 100 % of planned test cases executed | 68 / 68 executed | ✅ |
| All Critical / High defects fixed and retested | 4 / 4 closed | ✅ |
| Final pass rate ≥ 95 % | 100 % | ✅ |
| Test summary report prepared | This document | ✅ |

## 6. Observations

1. **Defects cluster at boundaries.** Two of the five defects (DEF-001, DEF-004) were off-by-one
   errors at the exact edge of a range. Values just inside and just outside the edges worked.
2. **State transition testing found sequence bugs.** DEF-002 shows up only with the event sequence
   fail → success → fail → fail, and DEF-003 only when an invalid event is sent in the SUBMITTED state.
3. **Testing every decision table column paid off.** DEF-005 appeared only in rules R7/R8,
   which a collapsed table with "don't care" entries would have merged with R3/R4.
4. **Equivalence partitioning found no defects** in this build. It still provided cheap, wide
   coverage of the valid and invalid input classes.
5. **Automation made regression cheap.** Re-running all 68 tests after the fixes took under a second,
   which confirmed that the fixes did not break other functionality.

## 7. Recommendation

The application meets all exit criteria for the scope of this test plan and is recommended for
release. Performance, security and UI testing (out of scope) should be planned for a later phase.

## 8. Report artefacts

| Artefact | Location |
|----------|----------|
| JUnit XML (Cycle 1 / Cycle 2) | [Cycle1](../03_Test_Execution/Cycle1_Results/TEST-junit-jupiter.xml) · [Cycle2](../03_Test_Execution/Cycle2_Results/TEST-junit-jupiter.xml) |
| HTML reports | [Cycle1](../03_Test_Execution/Cycle1_Results/Cycle1_Report.html) · [Cycle2](../03_Test_Execution/Cycle2_Results/Cycle2_Report.html) |
| Screenshots | [07_Screenshots](../07_Screenshots/README.md) |
