package com.oes.registration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Registration - Boundary Value Analysis")
class RegistrationBoundaryValueTest {

    @ParameterizedTest(name = "{0}: age {1} -> {2}")
    @DisplayName("Age (valid range 16-60 inclusive)")
    @CsvSource({
            "TC_REG_BVA_01, 15, INVALID_AGE",
            "TC_REG_BVA_02, 16, SUCCESS",
            "TC_REG_BVA_03, 17, SUCCESS",
            "TC_REG_BVA_04, 59, SUCCESS",
            "TC_REG_BVA_05, 60, SUCCESS",
            "TC_REG_BVA_06, 61, INVALID_AGE"
    })
    void ageBoundaries(String tcId, int age, RegistrationStatus expected) {
        assertEquals(expected, new RegistrationService()
                .register("Asha Verma", "asha@college.edu", age, "Exam2026"));
    }

    @ParameterizedTest(name = "{0}: password ''{1}'' (length {2}) -> {3}")
    @DisplayName("Password length (valid range 8-16)")
    @CsvSource({
            "TC_REG_BVA_07, Exam202,           7,  INVALID_PASSWORD",
            "TC_REG_BVA_08, Exam2026,          8,  SUCCESS",
            "TC_REG_BVA_09, Exam2026Exam2026,  16, SUCCESS",
            "TC_REG_BVA_10, Exam2026Exam20261, 17, INVALID_PASSWORD"
    })
    void passwordLengthBoundaries(String tcId, String password, int length, RegistrationStatus expected) {
        assertEquals(length, password.length(), "test data sanity check");
        assertEquals(expected, new RegistrationService()
                .register("Asha Verma", "asha@college.edu", 20, password));
    }

    @ParameterizedTest(name = "{0}: name of length {2} -> {3}")
    @DisplayName("Name length (valid range 3-30)")
    @CsvSource({
            "TC_REG_BVA_11, Al,                              2,  INVALID_NAME",
            "TC_REG_BVA_12, Ali,                             3,  SUCCESS",
            "TC_REG_BVA_13, Abcdefghijklmnopqrstuvwxyzabcd,  30, SUCCESS",
            "TC_REG_BVA_14, Abcdefghijklmnopqrstuvwxyzabcde, 31, INVALID_NAME"
    })
    void nameLengthBoundaries(String tcId, String name, int length, RegistrationStatus expected) {
        assertEquals(length, name.length(), "test data sanity check");
        assertEquals(expected, new RegistrationService()
                .register(name, "asha@college.edu", 20, "Exam2026"));
    }
}
