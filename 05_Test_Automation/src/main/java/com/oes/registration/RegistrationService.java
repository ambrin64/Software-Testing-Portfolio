package com.oes.registration;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Registers students for the Online Examination System.
 *
 * Rules (see Test Plan REQ-01..REQ-05):
 *   name     : 3-30 characters (trimmed)
 *   email    : local@domain.tld, unique (case-insensitive)
 *   age      : 16-60 inclusive
 *   password : 8-16 characters, at least one uppercase letter and one digit
 */
public class RegistrationService {

    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 30;
    public static final int MIN_AGE = 16;
    public static final int MAX_AGE = 60;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 16;

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private final Set<String> registeredEmails = new HashSet<>();

    public RegistrationStatus register(String name, String email, int age, String password) {
        if (name == null) {
            return RegistrationStatus.INVALID_NAME;
        }
        String trimmed = name.trim();
        if (trimmed.length() < MIN_NAME_LENGTH || trimmed.length() > MAX_NAME_LENGTH) {
            return RegistrationStatus.INVALID_NAME;
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            return RegistrationStatus.INVALID_EMAIL;
        }
        if (registeredEmails.contains(email.toLowerCase())) {
            return RegistrationStatus.DUPLICATE_EMAIL;
        }
        if (age < MIN_AGE || age >= MAX_AGE) {
            return RegistrationStatus.INVALID_AGE;
        }
        if (!isValidPassword(password)) {
            return RegistrationStatus.INVALID_PASSWORD;
        }
        registeredEmails.add(email.toLowerCase());
        return RegistrationStatus.SUCCESS;
    }

    private boolean isValidPassword(String password) {
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasUpper && hasDigit;
    }

    public boolean isRegistered(String email) {
        return email != null && registeredEmails.contains(email.toLowerCase());
    }
}
