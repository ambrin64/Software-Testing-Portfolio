# Conclusion

## What we did

We tested the core functionality of an **Online Examination System** across five modules
(Registration, Login, Exam Session, Scoring/Grading and Result) and followed the software testing
life cycle from start to finish:

1. **Test planning.** We wrote an IEEE 829 style test plan with 16 numbered requirements, scope,
   environment, entry/exit criteria and risks.
2. **Test design.** We derived **68 test cases** using Equivalence Partitioning, Boundary Value
   Analysis, Decision Tables and State Transition testing, and traced them to the requirements in an RTM.
3. **Automation.** We implemented every test case in **JUnit 5**, using parameterized tests for the
   BVA and decision table cases.
4. **Execution and defect management.** Cycle 1 gave 61/68 passed and found **5 defects** (1 Critical,
   3 High, 1 Medium). We documented each defect, fixed it, retested it and closed it.
5. **Regression.** Cycle 2 gave **68/68 passed (100 %)**, which met all exit criteria.

## Key learnings

- **Boundary value analysis is very effective at a low cost.** Two of the five defects (age 60,
  grade at 75 %) were at the exact edge of a range. A mid-range value from equivalence partitioning
  would never have found them.
- **State transition testing finds what single-input tests miss.** The login lock-out defect
  appeared only after a particular *sequence* of events, and the answer-after-submit defect only
  when an invalid event was sent in the SUBMITTED state.
- **Every decision table rule should be tested.** Merging "don't care" columns would have hidden the
  malpractice defect (rules R7/R8).
- **Automation makes regression testing practical.** Re-running all 68 tests took under a second, which
  gave us confidence that the fixes did not break anything else.
- **Traceability matters.** Using the same test case ID in the design documents, the JUnit display
  names, the execution logs and the defect reports let us follow any requirement through to its
  result.
- **Good test design can shape the code.** Injecting a fake clock made the 30-minute exam time-out
  testable in milliseconds.

## Limitations and future work

- Only functional, unit-level testing of business logic was performed. Future work could add
  **Selenium** UI tests for a web front end, **Postman** API tests, and **mutation testing** (e.g. PIT)
  to measure how strong the test suite is.
- Non-functional testing (performance under many simultaneous exam takers, security of answer
  submission) is out of scope and recommended for a later phase.

## Final statement

The Online Examination System meets all the requirements that were tested. Every defect found was
fixed and verified, and the GitHub repository records each stage of the testing process through
its commit history.
