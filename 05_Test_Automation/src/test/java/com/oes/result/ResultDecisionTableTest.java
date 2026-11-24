package com.oes.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Result Status - Decision Table")
class ResultDecisionTableTest {

    private final ResultCalculator calc = new ResultCalculator();

    @ParameterizedTest(name = "{0} ({1}): {2} %, malpractice={3}, allSections={4} -> {5}")
    @DisplayName("Decision table rules R1-R8")
    @CsvSource({
            "TC_RES_DT_01, R1, 65, false, true,  PASS",
            "TC_RES_DT_02, R2, 65, false, false, FAIL",
            "TC_RES_DT_03, R3, 65, true,  true,  WITHHELD",
            "TC_RES_DT_04, R4, 65, true,  false, WITHHELD",
            "TC_RES_DT_05, R5, 30, false, true,  FAIL",
            "TC_RES_DT_06, R6, 30, false, false, FAIL",
            "TC_RES_DT_07, R7, 30, true,  true,  WITHHELD",
            "TC_RES_DT_08, R8, 30, true,  false, WITHHELD"
    })
    void decisionTable(String tcId, String rule, double percentage, boolean malpractice,
                       boolean allSections, ResultStatus expected) {
        assertEquals(expected, calc.status(percentage, malpractice, allSections));
    }
}
