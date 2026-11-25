# Test Execution Log — Cycle 2 (Retest & Regression)

| Field | Value |
|-------|-------|
| Cycle | 2 (retesting of fixed defects + full regression) |
| Build | Commit `7333127` — "Fix DEF-001 to DEF-005 in application code" |
| Executed on | 24-Nov-2026 |
| Environment | Windows 11, Oracle JDK 21.0.11, JUnit 5 (Platform 1.11.4) |
| Execution method | Automated — `05_Test_Automation/run_tests.ps1` |
| Raw evidence | [console_output.txt](Cycle2_Results/console_output.txt), [TEST-junit-jupiter.xml](Cycle2_Results/TEST-junit-jupiter.xml), [Cycle2_Report.html](Cycle2_Results/Cycle2_Report.html) |

## Summary

| Total | Passed | Failed | Blocked | Pass rate |
|:-----:|:------:|:------:|:-------:|:---------:|
| 68 | 68 | 0 | 0 | 100 % |

## Retest of defects

| Defect | Retested with | Cycle 1 | Cycle 2 | Defect status |
|--------|---------------|:-------:|:-------:|---------------|
| DEF-001 | TC_REG_BVA_05 | ❌ FAIL | ✅ PASS | Closed |
| DEF-002 | TC_LGN_ST_05, TC_LGN_ST_06 | ❌ FAIL | ✅ PASS | Closed |
| DEF-003 | TC_EXM_ST_06 | ❌ FAIL | ✅ PASS | Closed |
| DEF-004 | TC_GRD_BVA_08 | ❌ FAIL | ✅ PASS | Closed |
| DEF-005 | TC_RES_DT_07, TC_RES_DT_08 | ❌ FAIL | ✅ PASS | Closed |

## Regression

All 61 test cases that passed in Cycle 1 were executed again and **still pass**. The fixes did not
introduce new defects (for example, TC_REG_BVA_06 (age 61) still returns INVALID_AGE and
TC_RES_DT_02 (≥ 40 % but sections missing) still returns FAIL).

## Detailed results (expected vs. actual)
| # | Test Case ID | Description | Expected Result | Actual Result | Status |
|---|--------------|-------------|-----------------|---------------|--------|
| 1 | TC_EXM_ST_01 | new session -> NOT_STARTED | NOT_STARTED | NOT_STARTED | ✅ PASS |
| 2 | TC_EXM_ST_02 | NOT_STARTED + start() -> IN_PROGRESS | IN_PROGRESS | IN_PROGRESS | ✅ PASS |
| 3 | TC_EXM_ST_03 | NOT_STARTED + answer() -> IllegalStateException | IllegalStateException | IllegalStateException | ✅ PASS |
| 4 | TC_EXM_ST_04 | NOT_STARTED + submit() -> IllegalStateException | IllegalStateException | IllegalStateException | ✅ PASS |
| 5 | TC_EXM_ST_05 | IN_PROGRESS + submit() -> SUBMITTED | SUBMITTED | SUBMITTED | ✅ PASS |
| 6 | TC_EXM_ST_06 | SUBMITTED + answer() -> IllegalStateException, answers unchanged | IllegalStateException, answers unchanged | IllegalStateException, answers unchanged | ✅ PASS |
| 7 | TC_EXM_ST_07 | IN_PROGRESS + duration elapsed -> TIMED_OUT | TIMED_OUT | TIMED_OUT | ✅ PASS |
| 8 | TC_EXM_ST_08 | TIMED_OUT + answer() -> IllegalStateException | IllegalStateException | IllegalStateException | ✅ PASS |
| 9 | TC_EXM_ST_09 | IN_PROGRESS + start() again -> IllegalStateException | IllegalStateException | IllegalStateException | ✅ PASS |
| 10 | TC_EXM_ST_10 | TIMED_OUT + submit() -> IllegalStateException | IllegalStateException | IllegalStateException | ✅ PASS |
| 11 | TC_EXM_ST_11 | IN_PROGRESS + answer() with invalid option -> IllegalArgumentException | IllegalArgumentException | IllegalArgumentException | ✅ PASS |
| 12 | TC_EXM_ST_12 | IN_PROGRESS + change answer -> last answer saved | last answer saved | last answer saved | ✅ PASS |
| 13 | TC_GRD_BVA_01 | -1 % -> IllegalArgumentException | IllegalArgumentException | IllegalArgumentException | ✅ PASS |
| 14 | TC_GRD_BVA_02 | 0 % -> F | F | F | ✅ PASS |
| 15 | TC_GRD_BVA_03 | 39 % -> F | F | F | ✅ PASS |
| 16 | TC_GRD_BVA_04 | 40 % -> C | C | C | ✅ PASS |
| 17 | TC_GRD_BVA_05 | 59 % -> C | C | C | ✅ PASS |
| 18 | TC_GRD_BVA_06 | 60 % -> B | B | B | ✅ PASS |
| 19 | TC_GRD_BVA_07 | 74 % -> B | B | B | ✅ PASS |
| 20 | TC_GRD_BVA_08 | 75 % -> A | A | A | ✅ PASS |
| 21 | TC_GRD_BVA_09 | 89 % -> A | A | A | ✅ PASS |
| 22 | TC_GRD_BVA_10 | 90 % -> A+ | A+ | A+ | ✅ PASS |
| 23 | TC_GRD_BVA_11 | 100 % -> A+ | A+ | A+ | ✅ PASS |
| 24 | TC_GRD_BVA_12 | 101 % -> IllegalArgumentException | IllegalArgumentException | IllegalArgumentException | ✅ PASS |
| 25 | TC_LGN_ST_01 | S0 + valid password -> SUCCESS, stays S0 | SUCCESS, stays S0 | SUCCESS, stays S0 | ✅ PASS |
| 26 | TC_LGN_ST_02 | S0 + invalid password -> INVALID_CREDENTIALS, S1 | INVALID_CREDENTIALS, S1 | INVALID_CREDENTIALS, S1 | ✅ PASS |
| 27 | TC_LGN_ST_03 | three invalid passwords -> ACCOUNT_LOCKED, S3 | ACCOUNT_LOCKED, S3 | ACCOUNT_LOCKED, S3 | ✅ PASS |
| 28 | TC_LGN_ST_04 | S3 (locked) + valid password -> ACCOUNT_LOCKED | ACCOUNT_LOCKED | ACCOUNT_LOCKED | ✅ PASS |
| 29 | TC_LGN_ST_05 | S2 + valid password -> SUCCESS and counter reset to 0 | SUCCESS and counter reset to 0 | SUCCESS and counter reset to 0 | ✅ PASS |
| 30 | TC_LGN_ST_06 | invalid, valid, invalid, invalid -> INVALID_CREDENTIALS, not locked | INVALID_CREDENTIALS, not locked | INVALID_CREDENTIALS, not locked | ✅ PASS |
| 31 | TC_LGN_ST_07 | S3 + admin unlock + valid password -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 32 | TC_LGN_ST_08 | unregistered user -> USER_NOT_FOUND | USER_NOT_FOUND | USER_NOT_FOUND | ✅ PASS |
| 33 | TC_REG_BVA_01 | age 15 -> INVALID_AGE | INVALID_AGE | INVALID_AGE | ✅ PASS |
| 34 | TC_REG_BVA_02 | age 16 -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 35 | TC_REG_BVA_03 | age 17 -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 36 | TC_REG_BVA_04 | age 59 -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 37 | TC_REG_BVA_05 | age 60 -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 38 | TC_REG_BVA_06 | age 61 -> INVALID_AGE | INVALID_AGE | INVALID_AGE | ✅ PASS |
| 39 | TC_REG_BVA_07 | password 'Exam202' (length 7) -> INVALID_PASSWORD | INVALID_PASSWORD | INVALID_PASSWORD | ✅ PASS |
| 40 | TC_REG_BVA_08 | password 'Exam2026' (length 8) -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 41 | TC_REG_BVA_09 | password 'Exam2026Exam2026' (length 16) -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 42 | TC_REG_BVA_10 | password 'Exam2026Exam20261' (length 17) -> INVALID_PASSWORD | INVALID_PASSWORD | INVALID_PASSWORD | ✅ PASS |
| 43 | TC_REG_BVA_11 | name of length 2 -> INVALID_NAME | INVALID_NAME | INVALID_NAME | ✅ PASS |
| 44 | TC_REG_BVA_12 | name of length 3 -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 45 | TC_REG_BVA_13 | name of length 30 -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 46 | TC_REG_BVA_14 | name of length 31 -> INVALID_NAME | INVALID_NAME | INVALID_NAME | ✅ PASS |
| 47 | TC_REG_EP_01 | all inputs valid -> SUCCESS | SUCCESS | SUCCESS | ✅ PASS |
| 48 | TC_REG_EP_02 | e-mail without '@' -> INVALID_EMAIL | INVALID_EMAIL | INVALID_EMAIL | ✅ PASS |
| 49 | TC_REG_EP_03 | e-mail without domain -> INVALID_EMAIL | INVALID_EMAIL | INVALID_EMAIL | ✅ PASS |
| 50 | TC_REG_EP_04 | empty name -> INVALID_NAME | INVALID_NAME | INVALID_NAME | ✅ PASS |
| 51 | TC_REG_EP_05 | password without digit -> INVALID_PASSWORD | INVALID_PASSWORD | INVALID_PASSWORD | ✅ PASS |
| 52 | TC_REG_EP_06 | password without uppercase -> INVALID_PASSWORD | INVALID_PASSWORD | INVALID_PASSWORD | ✅ PASS |
| 53 | TC_REG_EP_07 | already registered e-mail -> DUPLICATE_EMAIL | DUPLICATE_EMAIL | DUPLICATE_EMAIL | ✅ PASS |
| 54 | TC_REG_EP_08 | null e-mail -> INVALID_EMAIL | INVALID_EMAIL | INVALID_EMAIL | ✅ PASS |
| 55 | TC_RES_DT_01 | (R1): 65 %, malpractice=false, allSections=true -> PASS | PASS | PASS | ✅ PASS |
| 56 | TC_RES_DT_02 | (R2): 65 %, malpractice=false, allSections=false -> FAIL | FAIL | FAIL | ✅ PASS |
| 57 | TC_RES_DT_03 | (R3): 65 %, malpractice=true, allSections=true -> WITHHELD | WITHHELD | WITHHELD | ✅ PASS |
| 58 | TC_RES_DT_04 | (R4): 65 %, malpractice=true, allSections=false -> WITHHELD | WITHHELD | WITHHELD | ✅ PASS |
| 59 | TC_RES_DT_05 | (R5): 30 %, malpractice=false, allSections=true -> FAIL | FAIL | FAIL | ✅ PASS |
| 60 | TC_RES_DT_06 | (R6): 30 %, malpractice=false, allSections=false -> FAIL | FAIL | FAIL | ✅ PASS |
| 61 | TC_RES_DT_07 | (R7): 30 %, malpractice=true, allSections=true -> WITHHELD | WITHHELD | WITHHELD | ✅ PASS |
| 62 | TC_RES_DT_08 | (R8): 30 %, malpractice=true, allSections=false -> WITHHELD | WITHHELD | WITHHELD | ✅ PASS |
| 63 | TC_SCR_EP_01 | all answers correct -> 20 | 20 | 20 | ✅ PASS |
| 64 | TC_SCR_EP_02 | all answers wrong -> 0 (never negative) | 0 (never negative) | 0 (never negative) | ✅ PASS |
| 65 | TC_SCR_EP_03 | 3 correct, 1 wrong, 1 unanswered -> 11 | 11 | 11 | ✅ PASS |
| 66 | TC_SCR_EP_04 | no answers -> 0 | 0 | 0 | ✅ PASS |
| 67 | TC_SCR_EP_05 | score 11 of 20 -> 55.0 % | 55.0 % | 55.0 % | ✅ PASS |
| 68 | TC_SCR_EP_06 | section B unanswered -> allSectionsAttempted = false | allSectionsAttempted = false | allSectionsAttempted = false | ✅ PASS |
