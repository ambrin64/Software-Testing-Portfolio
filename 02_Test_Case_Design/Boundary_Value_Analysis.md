# Boundary Value Analysis (BVA)

Defects cluster at the edges of input ranges. For each range `[min, max]` we test
**min−1, min, min+1, max−1, max, max+1** (fewer where the inner values add nothing new).

---

## 1. Registration — Age (REQ-04: 16 to 60 inclusive)

```
   invalid   |<------------------ valid ------------------>|   invalid
 ----- 15 --- 16 --- 17 ---------------------------- 59 --- 60 --- 61 -----
```

| TC ID | Age | Boundary | Expected result |
|-------|-----|----------|-----------------|
| TC_REG_BVA_01 | 15 | min − 1 | INVALID_AGE |
| TC_REG_BVA_02 | 16 | min | SUCCESS |
| TC_REG_BVA_03 | 17 | min + 1 | SUCCESS |
| TC_REG_BVA_04 | 59 | max − 1 | SUCCESS |
| TC_REG_BVA_05 | 60 | max | SUCCESS |
| TC_REG_BVA_06 | 61 | max + 1 | INVALID_AGE |

## 2. Registration — Password length (REQ-05: 8 to 16 characters)

| TC ID | Password | Length | Boundary | Expected result |
|-------|----------|--------|----------|-----------------|
| TC_REG_BVA_07 | `Exam202` | 7 | min − 1 | INVALID_PASSWORD |
| TC_REG_BVA_08 | `Exam2026` | 8 | min | SUCCESS |
| TC_REG_BVA_09 | `Exam2026Exam2026` | 16 | max | SUCCESS |
| TC_REG_BVA_10 | `Exam2026Exam20261` | 17 | max + 1 | INVALID_PASSWORD |

## 3. Registration — Name length (REQ-01: 3 to 30 characters)

| TC ID | Name | Length | Boundary | Expected result |
|-------|------|--------|----------|-----------------|
| TC_REG_BVA_11 | `Al` | 2 | min − 1 | INVALID_NAME |
| TC_REG_BVA_12 | `Ali` | 3 | min | SUCCESS |
| TC_REG_BVA_13 | `Abcdefghijklmnopqrstuvwxyzabcd` | 30 | max | SUCCESS |
| TC_REG_BVA_14 | `Abcdefghijklmnopqrstuvwxyzabcde` | 31 | max + 1 | INVALID_NAME |

---

## 4. Grade bands (REQ-15)

| Band | Range | Grade |
|------|-------|-------|
| 1 | 90 – 100 | A+ |
| 2 | 75 – 89 | A |
| 3 | 60 – 74 | B |
| 4 | 40 – 59 | C |
| 5 | 0 – 39 | F |
| — | < 0 or > 100 | invalid |

```
 invalid | F        | C        | B        | A        | A+        | invalid
 ---- -1  0 ... 39   40 ... 59  60 ... 74  75 ... 89  90 ... 100  101 ----
```

| TC ID | Percentage | Boundary | Expected result |
|-------|-----------|----------|-----------------|
| TC_GRD_BVA_01 | −1 | below range | IllegalArgumentException |
| TC_GRD_BVA_02 | 0 | lower limit | F |
| TC_GRD_BVA_03 | 39 | F upper edge | F |
| TC_GRD_BVA_04 | 40 | C lower edge | C |
| TC_GRD_BVA_05 | 59 | C upper edge | C |
| TC_GRD_BVA_06 | 60 | B lower edge | B |
| TC_GRD_BVA_07 | 74 | B upper edge | B |
| TC_GRD_BVA_08 | 75 | A lower edge | A |
| TC_GRD_BVA_09 | 89 | A upper edge | A |
| TC_GRD_BVA_10 | 90 | A+ lower edge | A+ |
| TC_GRD_BVA_11 | 100 | upper limit | A+ |
| TC_GRD_BVA_12 | 101 | above range | IllegalArgumentException |
