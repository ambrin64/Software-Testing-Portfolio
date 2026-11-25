# DEF-004 — A score of exactly 75 % is graded B instead of A

| Field | Value |
|-------|-------|
| Defect ID | DEF-004 |
| Module | Grading |
| Requirement | REQ-15 — 75–89 % → grade A |
| Detected by | TC_GRD_BVA_08 (Boundary Value Analysis) |
| Detected in | Test Cycle 1, build `974dd41` |
| Severity | High |
| Priority | High |
| Status | **Closed** |
| Environment | Windows 11, JDK 21.0.11, JUnit 5.11 |

## Steps to reproduce
1. Call `ResultCalculator.grade(75)`.

## Expected result
`A`

## Actual result
`B`

```
TC_GRD_BVA_08: 75 % -> A ✘ expected: <A> but was: <B>
```

## Analysis
All other band edges (39/40, 59/60, 74, 89/90) are graded correctly. Only the lower edge of band A
is wrong: the comparison is exclusive (`> 75`) where the requirement says inclusive (`≥ 75`).
Students scoring exactly 75 % receive a lower grade, which directly affects their results.

## Evidence
- [Cycle 1 console output](../03_Test_Execution/Cycle1_Results/console_output.txt)
- [Cycle 1 HTML report](../03_Test_Execution/Cycle1_Results/Cycle1_Report.html)

## Resolution

| Field | Value |
|-------|-------|
| Fix | `ResultCalculator.grade()` used `percentage > 75`; changed to `percentage >= 75`. |
| Fixed in | Commit `7333127` |
| Retested with | TC_GRD_BVA_08 (plus regression TC_GRD_BVA_01–12) |
| Retest result | ✅ PASS in Cycle 2 ([log](../03_Test_Execution/Test_Execution_Log_Cycle2.md)) |
| Final status | Closed |
