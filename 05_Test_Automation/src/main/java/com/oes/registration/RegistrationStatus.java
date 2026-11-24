package com.oes.registration;

/** Outcome of a student registration attempt. */
public enum RegistrationStatus {
    SUCCESS,
    INVALID_NAME,
    INVALID_EMAIL,
    DUPLICATE_EMAIL,
    INVALID_AGE,
    INVALID_PASSWORD
}
