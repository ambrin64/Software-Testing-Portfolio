# DEF-001 — Student aged exactly 60 cannot register

| Field | Value |
|-------|-------|
| Defect ID | DEF-001 |
| Module | Registration |
| Requirement | REQ-04 — age must be between 16 and 60 (inclusive) |
| Detected by | TC_REG_BVA_05 (Boundary Value Analysis) |
| Detected in | Test Cycle 1, build `974dd41` |
| Severity | Medium |
| Priority | High |
| Status | **Closed** |
| Environment | Windows 11, JDK 21.0.11, JUnit 5.11 |

## Steps to reproduce
1. Open the registration service.
2. Register with name `Asha Verma`, e-mail `asha@college.edu`, age **60**, password `Exam2026`.

## Expected result
`SUCCESS`. 60 is the upper limit of the valid range and is included in it.

## Actual result
`INVALID_AGE`

```
TC_REG_BVA_05: age 60 -> SUCCESS ✘ expected: <SUCCESS> but was: <INVALID_AGE>
```

## Analysis
Ages 59 (max−1) and 61 (max+1) behave correctly, so only the exact boundary value is mishandled.
This is the classic **off-by-one error** that boundary value analysis is designed to catch.
Equivalence partitioning with a mid-range value (e.g. 35) would **not** have found it.

## Evidence
- [Cycle 1 console output](../03_Test_Execution/Cycle1_Results/console_output.txt)
- [Cycle 1 HTML report](../03_Test_Execution/Cycle1_Results/Cycle1_Report.html)

## Resolution

| Field | Value |
|-------|-------|
| Fix | `RegistrationService.register()` used `age >= MAX_AGE`; changed to `age > MAX_AGE` so 60 is inclusive. |
| Fixed in | Commit `0632372` |
| Retested with | TC_REG_BVA_05 (plus regression TC_REG_BVA_01–06) |
| Retest result | ✅ PASS in Cycle 2 ([log](../03_Test_Execution/Test_Execution_Log_Cycle2.md)) |
| Final status | Closed |
