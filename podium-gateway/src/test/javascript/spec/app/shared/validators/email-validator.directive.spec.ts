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
import { EmailValidatorDirective } from '../../../../../../main/webapp/app/shared/validators/email-validator.directive';

describe('EmailValidatorDirective (simple)', () => {

    let validator: EmailValidatorDirective;
    let invalidResultObj = {
        emailValidator: {
            valid : false
        }
    };

    beforeEach(() => {
        validator = new EmailValidatorDirective();
    });

    it('should return false when validate empty string', () => {
        expect(validator.validate(new FormControl(''))).toEqual(invalidResultObj);
    });

    it('should return false when validate empty string', () => {
        expect(validator.validate(new FormControl('xxx'))).toEqual(invalidResultObj);
    });

    it('should return false when validate empty string', () => {
        expect(validator.validate(new FormControl(undefined))).toEqual(invalidResultObj);
    });

    it('should return false when validate empty string', () => {
        expect(validator.validate(new FormControl('foo@bar'))).toEqual(null);
    });

});


