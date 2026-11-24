package com.oes.registration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Registration - Equivalence Partitioning")
class RegistrationEquivalencePartitioningTest {

    private RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationService();
    }

    @Test
    @DisplayName("TC_REG_EP_01: all inputs valid -> SUCCESS")
    void allValid() {
        assertEquals(RegistrationStatus.SUCCESS,
                service.register("Asha Verma", "asha@college.edu", 20, "Exam2026"));
    }

    @Test
    @DisplayName("TC_REG_EP_02: e-mail without '@' -> INVALID_EMAIL")
    void emailWithoutAt() {
        assertEquals(RegistrationStatus.INVALID_EMAIL,
                service.register("Asha Verma", "asha.college.edu", 20, "Exam2026"));
    }

    @Test
    @DisplayName("TC_REG_EP_03: e-mail without domain -> INVALID_EMAIL")
    void emailWithoutDomain() {
        assertEquals(RegistrationStatus.INVALID_EMAIL,
                service.register("Asha Verma", "asha@", 20, "Exam2026"));
    }

    @Test
    @DisplayName("TC_REG_EP_04: empty name -> INVALID_NAME")
    void emptyName() {
        assertEquals(RegistrationStatus.INVALID_NAME,
                service.register("", "asha@college.edu", 20, "Exam2026"));
    }

    @Test
    @DisplayName("TC_REG_EP_05: password without digit -> INVALID_PASSWORD")
    void passwordWithoutDigit() {
        assertEquals(RegistrationStatus.INVALID_PASSWORD,
                service.register("Asha Verma", "asha@college.edu", 20, "Examination"));
    }

    @Test
    @DisplayName("TC_REG_EP_06: password without uppercase -> INVALID_PASSWORD")
    void passwordWithoutUppercase() {
        assertEquals(RegistrationStatus.INVALID_PASSWORD,
                service.register("Asha Verma", "asha@college.edu", 20, "exam2026"));
    }

    @Test
    @DisplayName("TC_REG_EP_07: already registered e-mail -> DUPLICATE_EMAIL")
    void duplicateEmail() {
        service.register("Asha Verma", "asha@college.edu", 20, "Exam2026");
        assertEquals(RegistrationStatus.DUPLICATE_EMAIL,
                service.register("Asha Verma", "asha@college.edu", 20, "Exam2026"));
    }

    @Test
    @DisplayName("TC_REG_EP_08: null e-mail -> INVALID_EMAIL")
    void nullEmail() {
        assertEquals(RegistrationStatus.INVALID_EMAIL,
                service.register("Asha Verma", null, 20, "Exam2026"));
    }
}
