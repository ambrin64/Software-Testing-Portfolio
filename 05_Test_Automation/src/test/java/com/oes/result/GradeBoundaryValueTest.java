package com.oes.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Grading - Boundary Value Analysis")
class GradeBoundaryValueTest {

    private final ResultCalculator calc = new ResultCalculator();

    @Test
    @DisplayName("TC_GRD_BVA_01: -1 % -> IllegalArgumentException")
    void belowRange() {
        assertThrows(IllegalArgumentException.class, () -> calc.grade(-1));
    }

    @ParameterizedTest(name = "{0}: {1} % -> {2}")
    @DisplayName("Grade band edges")
    @CsvSource({
            "TC_GRD_BVA_02, 0,   F",
            "TC_GRD_BVA_03, 39,  F",
            "TC_GRD_BVA_04, 40,  C",
            "TC_GRD_BVA_05, 59,  C",
            "TC_GRD_BVA_06, 60,  B",
            "TC_GRD_BVA_07, 74,  B",
            "TC_GRD_BVA_08, 75,  A",
            "TC_GRD_BVA_09, 89,  A",
            "TC_GRD_BVA_10, 90,  A+",
            "TC_GRD_BVA_11, 100, A+"
    })
    void gradeBands(String tcId, double percentage, String expectedGrade) {
        assertEquals(expectedGrade, calc.grade(percentage));
    }

    @Test
    @DisplayName("TC_GRD_BVA_12: 101 % -> IllegalArgumentException")
    void aboveRange() {
        assertThrows(IllegalArgumentException.class, () -> calc.grade(101));
    }
}
