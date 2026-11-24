# DEF-003 — Answers can be changed after the exam has been submitted

| Field | Value |
|-------|-------|
| Defect ID | DEF-003 |
| Module | Exam Session |
| Requirement | REQ-12 — after submission no further answers are accepted |
| Detected by | TC_EXM_ST_06 (State Transition — invalid event in SUBMITTED state) |
| Detected in | Test Cycle 1, build `974dd41` |
| Severity | **Critical** |
| Priority | High |
| Status | **Open** |
| Environment | Windows 11, JDK 21.0.11, JUnit 5.11 |

## Steps to reproduce
1. Create an exam session with 3 questions and start it.
2. Answer Q1 with option 0.
3. Submit the exam (state = SUBMITTED).
4. Call `answer(2, 1)`.

## Expected result
`IllegalStateException`. The saved answers stay `{1=0}`.

## Actual result
No exception is thrown and the answer to Q2 is saved after submission.

```
TC_EXM_ST_06 ✘ answer accepted after submission ==>
              Expected java.lang.IllegalStateException to be thrown, but nothing was thrown.
```

## Analysis
`ExamSession.answer()` rejects the NOT_STARTED and TIMED_OUT states but not SUBMITTED. Students
could change their answers after submitting, which breaks exam integrity, so the defect is
rated Critical. The invalid-transition cells of the state table (marked ✗) are what led to
this test case.

## Evidence
- [Cycle 1 console output](../03_Test_Execution/Cycle1_Results/console_output.txt)
- [Cycle 1 HTML report](../03_Test_Execution/Cycle1_Results/Cycle1_Report.html)
