# Decision Table Testing — Result Status (REQ-16)

The final result of an exam depends on a combination of three conditions, which makes it a natural
fit for a decision table.

**Business rule (REQ-16):**
- If malpractice is reported, the result is **WITHHELD** regardless of marks.
- Otherwise the student **PASSES** only if percentage ≥ 40 **and** every section was attempted.
- In all other cases the student **FAILS**.

## 1. Conditions and actions

| | Description |
|---|---|
| **C1** | Percentage ≥ 40 |
| **C2** | Malpractice reported |
| **C3** | All sections attempted |
| **A1** | Result = PASS |
| **A2** | Result = FAIL |
| **A3** | Result = WITHHELD |

## 2. Full decision table (2³ = 8 rules)

| | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 |
|---|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| C1: Percentage ≥ 40 | T | T | T | T | F | F | F | F |
| C2: Malpractice | F | F | T | T | F | F | T | T |
| C3: All sections attempted | T | F | T | F | T | F | T | F |
| **A1: PASS** | ✔ | | | | | | | |
| **A2: FAIL** | | ✔ | | | ✔ | ✔ | | |
| **A3: WITHHELD** | | | ✔ | ✔ | | | ✔ | ✔ |

> The table could be collapsed (C2 = T makes C1 and C3 "don't care"), but all 8 rules are kept as
> test cases. Testing the collapsed rules individually is what reveals ordering mistakes in the code.

## 3. Test cases

A percentage of **65** is used for C1 = T and **30** for C1 = F.

| TC ID | Rule | Percentage | Malpractice | All sections | Expected result |
|-------|------|-----------|-------------|--------------|-----------------|
| TC_RES_DT_01 | R1 | 65 | No | Yes | PASS |
| TC_RES_DT_02 | R2 | 65 | No | No | FAIL |
| TC_RES_DT_03 | R3 | 65 | Yes | Yes | WITHHELD |
| TC_RES_DT_04 | R4 | 65 | Yes | No | WITHHELD |
| TC_RES_DT_05 | R5 | 30 | No | Yes | FAIL |
| TC_RES_DT_06 | R6 | 30 | No | No | FAIL |
| TC_RES_DT_07 | R7 | 30 | Yes | Yes | WITHHELD |
| TC_RES_DT_08 | R8 | 30 | Yes | No | WITHHELD |
