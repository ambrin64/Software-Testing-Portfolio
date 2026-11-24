# Equivalence Partitioning (EP)

Equivalence partitioning divides the input domain into classes whose members are expected to be
handled the same way by the system. One representative value from each class is tested.

---

## 1. Student Registration (REQ-01 … REQ-05)

Base valid data: name `Asha Verma`, e-mail `asha@college.edu`, age `20`, password `Exam2026`.

### 1.1 Partitions identified

| Input | Valid classes | Invalid classes |
|-------|---------------|-----------------|
| Name | V1: 3–30 characters | I1: empty / blank · I2: < 3 chars · I3: > 30 chars · I4: null |
| E-mail | V2: `local@domain.tld` format, not yet registered | I5: no `@` · I6: no domain after `@` · I7: null · I8: already registered |
| Age | V3: 16–60 | I9: < 16 · I10: > 60 |
| Password | V4: 8–16 chars with ≥ 1 uppercase and ≥ 1 digit | I11: no digit · I12: no uppercase · I13: < 8 chars · I14: > 16 chars |

> Length-based classes (I2, I3, I9, I10, I13, I14) are covered more precisely by
> [Boundary Value Analysis](Boundary_Value_Analysis.md).

### 1.2 Test cases

| TC ID | Class(es) covered | Name | E-mail | Age | Password | Expected result |
|-------|-------------------|------|--------|-----|----------|-----------------|
| TC_REG_EP_01 | V1, V2, V3, V4 | Asha Verma | asha@college.edu | 20 | Exam2026 | SUCCESS |
| TC_REG_EP_02 | I5 | Asha Verma | asha.college.edu | 20 | Exam2026 | INVALID_EMAIL |
| TC_REG_EP_03 | I6 | Asha Verma | asha@ | 20 | Exam2026 | INVALID_EMAIL |
| TC_REG_EP_04 | I1 | _(empty)_ | asha@college.edu | 20 | Exam2026 | INVALID_NAME |
| TC_REG_EP_05 | I11 | Asha Verma | asha@college.edu | 20 | Examination | INVALID_PASSWORD |
| TC_REG_EP_06 | I12 | Asha Verma | asha@college.edu | 20 | exam2026 | INVALID_PASSWORD |
| TC_REG_EP_07 | I8 | Asha Verma (2nd time) | asha@college.edu | 20 | Exam2026 | DUPLICATE_EMAIL |
| TC_REG_EP_08 | I7 | Asha Verma | _null_ | 20 | Exam2026 | INVALID_EMAIL |

---

## 2. Score Calculation (REQ-14)

Exam of 5 questions, +4 per correct answer, −1 per wrong answer, 0 for unanswered, score never below 0.
Maximum score = 20.

### 2.1 Partitions identified

| Partition | Description |
|-----------|-------------|
| P1 | All answers correct |
| P2 | All answers wrong (raw score negative → must be floored at 0) |
| P3 | Mix of correct, wrong and unanswered |
| P4 | No answers at all |
| P5 | Percentage computation from a score |
| P6 | Section coverage — one whole section left unanswered |

### 2.2 Test cases

| TC ID | Partition | Input | Expected result |
|-------|-----------|-------|-----------------|
| TC_SCR_EP_01 | P1 | 5 correct | Score = 20 |
| TC_SCR_EP_02 | P2 | 5 wrong (raw −5) | Score = 0 |
| TC_SCR_EP_03 | P3 | 3 correct, 1 wrong, 1 unanswered | Score = 11 |
| TC_SCR_EP_04 | P4 | No answers | Score = 0 |
| TC_SCR_EP_05 | P5 | Score 11 of 20 | Percentage = 55.0 |
| TC_SCR_EP_06 | P6 | Sections A and B; only section A answered | allSectionsAttempted = false |
