# DEF-002 — Failed-login counter is not reset after a successful login

| Field | Value |
|-------|-------|
| Defect ID | DEF-002 |
| Module | Login / Authentication |
| Requirement | REQ-08 — a successful login resets the failed-attempt counter to 0 |
| Detected by | TC_LGN_ST_05, TC_LGN_ST_06 (State Transition) |
| Detected in | Test Cycle 1, build `974dd41` |
| Severity | High |
| Priority | High |
| Status | **Closed** |
| Environment | Windows 11, JDK 21.0.11, JUnit 5.11 |

## Steps to reproduce
1. Register user `asha@college.edu` / `Exam2026`.
2. Log in with a wrong password (`Wrong123`) → INVALID_CREDENTIALS.
3. Log in with the correct password → SUCCESS.
4. Log in with the wrong password twice.

## Expected result
After step 3 the failed-attempt counter is 0 (transition S1 → S0). Step 4 brings it to 2, so the
last attempt returns `INVALID_CREDENTIALS` and the account stays **unlocked**.

## Actual result
The counter keeps its old value after a successful login. The last attempt in step 4 returns
`ACCOUNT_LOCKED`, so a genuine student is locked out after only 2 consecutive failures.

```
TC_LGN_ST_05 ✘ failed attempts after successful login ==> expected: <0> but was: <2>
TC_LGN_ST_06 ✘ expected: <INVALID_CREDENTIALS> but was: <ACCOUNT_LOCKED>
             ✘ account should not be locked ==> expected: <false> but was: <true>
```

## Analysis
The transitions S1 → S0 and S2 → S0 on a valid password are not implemented, so failures add up
over the account's whole lifetime instead of counting only consecutive ones. Testing each state
on its own (TC_LGN_ST_01 … 04) cannot show this. Only an event *sequence* that passes through a
success state exposes it, which is why state transition testing found it.

## Evidence
- [Cycle 1 console output](../03_Test_Execution/Cycle1_Results/console_output.txt)
- [Cycle 1 HTML report](../03_Test_Execution/Cycle1_Results/Cycle1_Report.html)

## Resolution

| Field | Value |
|-------|-------|
| Fix | `AuthService.login()` now sets `failedAttempts = 0` on a successful login. |
| Fixed in | Commit `7333127` |
| Retested with | TC_LGN_ST_05, TC_LGN_ST_06 (plus regression TC_LGN_ST_01–08) |
| Retest result | ✅ PASS in Cycle 2 ([log](../03_Test_Execution/Test_Execution_Log_Cycle2.md)) |
| Final status | Closed |
