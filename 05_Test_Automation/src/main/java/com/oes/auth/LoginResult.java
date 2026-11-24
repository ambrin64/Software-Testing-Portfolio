package com.oes.auth;

/** Outcome of a login attempt. */
public enum LoginResult {
    SUCCESS,
    INVALID_CREDENTIALS,
    ACCOUNT_LOCKED,
    USER_NOT_FOUND
}
