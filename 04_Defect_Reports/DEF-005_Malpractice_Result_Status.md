# DEF-005 — Malpractice cases below 40 % show FAIL instead of WITHHELD

| Field | Value |
|-------|-------|
| Defect ID | DEF-005 |
| Module | Result status |
| Requirement | REQ-16 — malpractice → WITHHELD regardless of marks |
| Detected by | TC_RES_DT_07, TC_RES_DT_08 (Decision Table rules R7, R8) |
| Detected in | Test Cycle 1, build `974dd41` |
| Severity | High |
| Priority | Medium |
| Status | **Closed** |
| Environment | Windows 11, JDK 21.0.11, JUnit 5.11 |

## Steps to reproduce
1. Call `ResultCalculator.status(30, true, true)` (30 %, malpractice reported, all sections attempted).
2. Call `ResultCalculator.status(30, true, false)`.

## Expected result
`WITHHELD` in both cases (rules R7 and R8).

## Actual result
`FAIL` in both cases.

```
TC_RES_DT_07 (R7) ✘ expected: <WITHHELD> but was: <FAIL>
TC_RES_DT_08 (R8) ✘ expected: <WITHHELD> but was: <FAIL>
```

## Analysis
The code checks the pass percentage **before** the malpractice flag, so a student below 40 %
gets FAIL without the malpractice case being recorded or sent for review. Rules R3/R4 (≥ 40 % with
malpractice) work, so testing only those rules would have missed this. The defect shows why
every column of the decision table needs to be tested, including the "don't care" ones.

## Evidence
- [Cycle 1 console output](../03_Test_Execution/Cycle1_Results/console_output.txt)
- [Cycle 1 HTML report](../03_Test_Execution/Cycle1_Results/Cycle1_Report.html)

## Resolution

| Field | Value |
|-------|-------|
| Fix | `ResultCalculator.status()` now checks the malpractice flag first, then percentage and section coverage. |
| Fixed in | Commit `0632372` |
| Retested with | TC_RES_DT_07, TC_RES_DT_08 (plus regression TC_RES_DT_01–08) |
| Retest result | ✅ PASS in Cycle 2 ([log](../03_Test_Execution/Test_Execution_Log_Cycle2.md)) |
| Final status | Closed |
