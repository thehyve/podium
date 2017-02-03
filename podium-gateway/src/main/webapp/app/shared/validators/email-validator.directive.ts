import { Directive, forwardRef } from '@angular/core';
import { NG_VALIDATORS, AbstractControl, ValidatorFn, Validator, FormControl } from '@angular/forms';


// validation function
function validateEmailFactory(): ValidatorFn {
    return (c: AbstractControl) => {

        // tslint:disable max-length
        let pattern = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
        // tslint:enable
        let isValid =pattern.test(c.value);

        if(isValid) {
            return null;
        } else {
            return {
                emailValidator: {
                    valid: false
                }
            };
        }
    };
}

@Directive({
    selector: '[emailValidator][ngModel]',
    providers: [
        { provide: NG_VALIDATORS, useExisting: EmailValidatorDirective, multi: true }
    ]
})

export class EmailValidatorDirective implements Validator {
    validator: ValidatorFn;

    constructor() {
        this.validator = validateEmailFactory();
    }

    validate(c: FormControl) {
        return this.validator(c);
    }

}
