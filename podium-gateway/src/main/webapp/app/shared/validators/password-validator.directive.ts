/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Directive } from '@angular/core';
import { NG_VALIDATORS, AbstractControl, ValidatorFn, Validator, FormControl } from '@angular/forms';


// validation function
function validatePasswordFactory(): ValidatorFn {
    return (c: AbstractControl) => {

        // (                        # Start of group
        //  (?=.*\d)                # must contains one digit from 0-9
        //  (?=.*[@A-Za-z])         # must contains one lowercase characters
        //  (?=.*[^a-zA-Z0-9 ])     # must contains one special symbols in the list "@#$%"
        //  .                       # match anything with previous condition checking
        //  {8}                     # length at least 8 characters
        // )                        # End of group
        let pattern = /((?=.*\d)(?=.*[@A-Za-z])(?=.*[^a-zA-Z0-9 ]).{8})/;
        let isValid = pattern.test(c.value);

        if (isValid) {
            return null;
        } else {
            return {
                passwordValidator: {
                    valid: false
                }
            };
        }
    };
}

@Directive({
    selector: '[passwordValidator][ngModel]',
    providers: [
        {provide: NG_VALIDATORS, useExisting: PasswordValidatorDirective, multi: true}
    ]
})

export class PasswordValidatorDirective implements Validator {
    validator: ValidatorFn;

    constructor() {
        this.validator = validatePasswordFactory();
    }

    validate(c: FormControl) {
        return this.validator(c);
    }

}
