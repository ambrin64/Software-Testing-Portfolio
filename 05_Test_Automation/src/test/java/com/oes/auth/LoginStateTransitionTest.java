package com.oes.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Login - State Transition")
class LoginStateTransitionTest {

    private static final String USER = "asha@college.edu";
    private static final String PASSWORD = "Exam2026";
    private static final String WRONG = "Wrong123";

    private AuthService auth;

    @BeforeEach
    void setUp() {
        auth = new AuthService();
        auth.addUser(USER, PASSWORD);
    }

    @Test
    @DisplayName("TC_LGN_ST_01: S0 + valid password -> SUCCESS, stays S0")
    void validLoginFromInitialState() {
        assertEquals(LoginResult.SUCCESS, auth.login(USER, PASSWORD));
        assertEquals(0, auth.getFailedAttempts(USER));
    }

    @Test
    @DisplayName("TC_LGN_ST_02: S0 + invalid password -> INVALID_CREDENTIALS, S1")
    void oneFailure() {
        assertEquals(LoginResult.INVALID_CREDENTIALS, auth.login(USER, WRONG));
        assertEquals(1, auth.getFailedAttempts(USER));
        assertFalse(auth.isLocked(USER));
    }

    @Test
    @DisplayName("TC_LGN_ST_03: three invalid passwords -> ACCOUNT_LOCKED, S3")
    void threeFailuresLockAccount() {
        auth.login(USER, WRONG);
        auth.login(USER, WRONG);
        assertEquals(LoginResult.ACCOUNT_LOCKED, auth.login(USER, WRONG));
        assertTrue(auth.isLocked(USER));
    }

    @Test
    @DisplayName("TC_LGN_ST_04: S3 (locked) + valid password -> ACCOUNT_LOCKED")
    void lockedAccountRejectsValidPassword() {
        for (int i = 0; i < 3; i++) {
            auth.login(USER, WRONG);
        }
        assertEquals(LoginResult.ACCOUNT_LOCKED, auth.login(USER, PASSWORD));
    }

    @Test
    @DisplayName("TC_LGN_ST_05: S2 + valid password -> SUCCESS and counter reset to 0")
    void successfulLoginResetsCounter() {
        auth.login(USER, WRONG);
        auth.login(USER, WRONG);
        assertAll(
                () -> assertEquals(LoginResult.SUCCESS, auth.login(USER, PASSWORD)),
                () -> assertEquals(0, auth.getFailedAttempts(USER), "failed attempts after successful login"));
    }

    @Test
    @DisplayName("TC_LGN_ST_06: invalid, valid, invalid, invalid -> INVALID_CREDENTIALS, not locked")
    void failuresAreNotCarriedOverAfterSuccess() {
        auth.login(USER, WRONG);
        auth.login(USER, PASSWORD);
        auth.login(USER, WRONG);
        assertAll(
                () -> assertEquals(LoginResult.INVALID_CREDENTIALS, auth.login(USER, WRONG)),
                () -> assertFalse(auth.isLocked(USER), "account should not be locked"));
    }

    @Test
    @DisplayName("TC_LGN_ST_07: S3 + admin unlock + valid password -> SUCCESS")
    void adminUnlock() {
        for (int i = 0; i < 3; i++) {
            auth.login(USER, WRONG);
        }
        auth.unlock(USER);
        assertFalse(auth.isLocked(USER));
        assertEquals(LoginResult.SUCCESS, auth.login(USER, PASSWORD));
    }

    @Test
    @DisplayName("TC_LGN_ST_08: unregistered user -> USER_NOT_FOUND")
    void unknownUser() {
        assertEquals(LoginResult.USER_NOT_FOUND, auth.login("ghost@college.edu", PASSWORD));
    }
}
