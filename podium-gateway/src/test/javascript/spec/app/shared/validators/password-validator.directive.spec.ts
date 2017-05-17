/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { FormControl } from '@angular/forms';
import { PasswordValidatorDirective } from '../../../../../../main/webapp/app/shared/validators/password-validator.directive';

describe('PasswordValidatorDirective (simple)', () => {

    let validator: PasswordValidatorDirective;
    let invalidResultObj = {
        passwordValidator: {
            valid : false
        }
    };

    beforeEach(() => {
        validator = new PasswordValidatorDirective();
    });

    it('should return false when validate empty string', () => {
        expect(validator.validate(new FormControl(''))).toEqual(invalidResultObj);
    });

    it('should return false when validate undefined', () => {
        expect(validator.validate(new FormControl(undefined))).toEqual(invalidResultObj);
    });

    it('should return false when validate <8 char length  string', () => {
        expect(validator.validate(new FormControl('x1@'))).toEqual(invalidResultObj);
    });

    it('should return false when validate alphanumeric only password  string', () => {
        expect(validator.validate(new FormControl('qqqq1111'))).toEqual(invalidResultObj);
    });

    it('should return null when validate valid password string', () => {
        expect(validator.validate(new FormControl('qqq111!@#'))).toEqual(null);
    });

});


