# 02 — Test Case Design

Test cases were derived from the requirements in the [Test Plan](../01_Test_Plan/Test_Plan.md)
using four black-box test design techniques.

| Technique | Document | Module(s) | No. of test cases |
|-----------|----------|-----------|:-----------------:|
| Equivalence Partitioning | [Equivalence_Partitioning.md](Equivalence_Partitioning.md) | Registration, Score calculation | 14 |
| Boundary Value Analysis | [Boundary_Value_Analysis.md](Boundary_Value_Analysis.md) | Registration (age, password, name), Grading | 26 |
| Decision Table | [Decision_Table.md](Decision_Table.md) | Result status | 8 |
| State Transition | [State_Transition.md](State_Transition.md) | Login lock-out, Exam session | 20 |
| **Total** | | | **68** |

## Test case ID convention

`TC_<MODULE>_<TECHNIQUE>_<NN>`, for example `TC_REG_BVA_05` is Registration · Boundary Value Analysis · case 5.

| Module code | Module | | Technique code | Technique |
|-------------|--------|-|----------------|-----------|
| REG | Registration | | EP | Equivalence Partitioning |
| LGN | Login | | BVA | Boundary Value Analysis |
| EXM | Exam session | | DT | Decision Table |
| SCR | Score calculation | | ST | State Transition |
| GRD | Grading | | | |
| RES | Result status | | | |

Each test case ID is also used as the `@DisplayName` of the matching JUnit test in
[05_Test_Automation](../05_Test_Automation), so every designed case can be traced to its automated script and its execution result.

## Requirement Traceability Matrix (RTM)

| Requirement | Description | Test cases |
|-------------|-------------|------------|
| REQ-01 | Name 3–30 chars | TC_REG_EP_04, TC_REG_BVA_11 – 14 |
| REQ-02 | Valid e-mail format | TC_REG_EP_02, 03, 08 |
| REQ-03 | Unique e-mail | TC_REG_EP_07 |
| REQ-04 | Age 16–60 inclusive | TC_REG_BVA_01 – 06 |
| REQ-05 | Password rules | TC_REG_EP_05, 06, TC_REG_BVA_07 – 10 |
| REQ-06 | Valid login | TC_LGN_ST_01, 08 |
| REQ-07 | Lock after 3 failures | TC_LGN_ST_02, 03, 04 |
| REQ-08 | Successful login resets counter | TC_LGN_ST_05, 06 |
| REQ-09 | Locked until admin unlock | TC_LGN_ST_04, 07 |
| REQ-10 | Start before answer / submit | TC_EXM_ST_01 – 04, 09 |
| REQ-11 | Change answer while in progress | TC_EXM_ST_11, 12 |
| REQ-12 | No answers after submission | TC_EXM_ST_05, 06 |
| REQ-13 | Auto time-out | TC_EXM_ST_07, 08, 10 |
| REQ-14 | Scoring with negative marking | TC_SCR_EP_01 – 06 |
| REQ-15 | Grade bands | TC_GRD_BVA_01 – 12 |
| REQ-16 | PASS / FAIL / WITHHELD | TC_RES_DT_01 – 08 |

All 16 requirements are covered by at least one test case.
