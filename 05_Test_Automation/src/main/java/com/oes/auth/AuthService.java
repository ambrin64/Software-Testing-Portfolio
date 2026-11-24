package com.oes.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * Authenticates students (Test Plan REQ-06..REQ-09).
 *
 * Account states: ACTIVE(0 failures) -> ACTIVE(1) -> ACTIVE(2) -> LOCKED.
 * A successful login resets the failure counter; an administrator can unlock a locked account.
 */
public class AuthService {

    public static final int MAX_FAILED_ATTEMPTS = 3;

    private static final class Account {
        final String password;
        int failedAttempts;
        boolean locked;

        Account(String password) {
            this.password = password;
        }
    }

    private final Map<String, Account> accounts = new HashMap<>();

    public void addUser(String username, String password) {
        accounts.put(username, new Account(password));
    }

    public LoginResult login(String username, String password) {
        Account account = accounts.get(username);
        if (account == null) {
            return LoginResult.USER_NOT_FOUND;
        }
        if (account.locked) {
            return LoginResult.ACCOUNT_LOCKED;
        }
        if (account.password.equals(password)) {
            return LoginResult.SUCCESS;
        }
        account.failedAttempts++;
        if (account.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            account.locked = true;
            return LoginResult.ACCOUNT_LOCKED;
        }
        return LoginResult.INVALID_CREDENTIALS;
    }

    /** Administrator action: unlock the account and clear its failure counter. */
    public void unlock(String username) {
        Account account = accounts.get(username);
        if (account != null) {
            account.locked = false;
            account.failedAttempts = 0;
        }
    }

    public boolean isLocked(String username) {
        Account account = accounts.get(username);
        return account != null && account.locked;
    }

    public int getFailedAttempts(String username) {
        Account account = accounts.get(username);
        return account == null ? 0 : account.failedAttempts;
    }
}
