/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static Logger log = LoggerFactory.getLogger(PasswordValidator.class);

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 1000;

    private static final String numericalRegex = "(?=.*[0-9])"; // at least one numerical
    private static final String alphabeticalRegex = "(?=.*[a-zA-Z])"; // at least one alphabetical
    private static final String specialCharsRegex = "(?=.*[^a-zA-Z0-9 ])"; // at least one special chars

    private static final Pattern numericalPattern = Pattern.compile(numericalRegex);
    private static final Pattern alphabeticalPattern = Pattern.compile(alphabeticalRegex);
    private static final Pattern specialsCharsPattern = Pattern.compile(specialCharsRegex);

    /**
     * Password should contain the following types of characters:
     * - alphabetical lower or capital letters
     * - numerical chars
     * - special chars (not numerical or alphabetical)
     *
     * @param password
     * @return true if valid, false otherwise.
     */
    public static boolean validate(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        Matcher numericalMatcher = numericalPattern.matcher(password);
        Matcher alphabeticalMatcher = alphabeticalPattern.matcher(password);
        Matcher specialCharsMatcher = specialsCharsPattern.matcher(password);

        log.info("Password: {}, l = {}, special = {}, alpha = {}, numeric = {}",
            password, password.length(),
            specialCharsMatcher.find(),
            alphabeticalMatcher.find(),
            numericalMatcher.find());

        return password.length() >= MIN_PASSWORD_LENGTH &&
            password.length() <= MAX_PASSWORD_LENGTH &&
            specialCharsMatcher.find() &&
            alphabeticalMatcher.find() &&
            numericalMatcher.find();
    }

    @Override
    public void initialize(ValidPassword validPassword) {

    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return validate(password);
    }

}

