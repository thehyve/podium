/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */
import { FormControl } from '@angular/forms';
import { PasswordMatchesDirective } from '../../../../../../main/webapp/app/shared/validators/password-matches.directive';

describe('PasswordMatchesDirective', () => {

    let validator: PasswordMatchesDirective;
    let passwordField: FormControl;
    let confirmPasswordField: FormControl;

    let invalidResultObj = {
        pdmPasswordMatches: {
            valid : false
        }
    };

    beforeEach(() => {
        passwordField = new FormControl('');
        validator = new PasswordMatchesDirective();
        validator.pdmPasswordMatches = passwordField;
        confirmPasswordField = new FormControl('');
    });

    it('should return null when validating empty input', () => {
        expect(validator.validate(confirmPasswordField)).toEqual(null);
    });

    it('should return null when validating empty input, even when the reference input is not', () => {
        passwordField.setValue('Password123!');
        expect(validator.validate(confirmPasswordField)).toEqual(null);
    });

    it('should return null when validating equal inputs', () => {
        passwordField.setValue('Password123!');
        confirmPasswordField.setValue('Password123!');
        expect(validator.validate(confirmPasswordField)).toEqual(null);
    });

    it('should return false when validating different inputs', () => {
        confirmPasswordField.setValue('Password123!');
        expect(validator.validate(confirmPasswordField)).toEqual(invalidResultObj);
        passwordField.setValue('Password');
        expect(validator.validate(confirmPasswordField)).toEqual(invalidResultObj);
        passwordField.setValue('Password123');
        expect(validator.validate(confirmPasswordField)).toEqual(invalidResultObj);
    });

    it('should return false when validating different inputs after reference update', () => {
        passwordField.setValue('Password123!');
        confirmPasswordField.setValue('Password123!');
        expect(validator.validate(confirmPasswordField)).toEqual(null);
        passwordField.setValue('Other value');
        expect(validator.validate(confirmPasswordField)).toEqual(invalidResultObj);
    });

    it('should return false when validating different inputs after input update', () => {
        passwordField.setValue('Password123!');
        confirmPasswordField.setValue('Password123!');
        expect(validator.validate(confirmPasswordField)).toEqual(null);
        confirmPasswordField.setValue('Password123?');
        expect(validator.validate(confirmPasswordField)).toEqual(invalidResultObj);
    });

});


