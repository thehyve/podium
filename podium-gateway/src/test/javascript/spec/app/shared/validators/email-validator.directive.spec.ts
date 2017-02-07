import { AbstractControl, FormControl } from '@angular/forms';
import { EmailValidatorDirective } from '../../../../../../main/webapp/app/shared/validators/email-validator.directive'

describe('EmailValidatorDirective (simple)', () => {

    let validator : EmailValidatorDirective;
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


