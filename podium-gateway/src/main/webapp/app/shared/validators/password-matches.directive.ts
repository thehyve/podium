/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */
import { Directive, Input } from '@angular/core';
import { NG_VALIDATORS, Validator, FormControl } from '@angular/forms';

/**
 * Matching passwords directive. Validates that the control value matches the value of another control.
 *
 * Example:
 * {@code
 *  <input type="password" id="password" name="password" #password="ngModel"
 *      [(ngModel)]="registerAccount.password">
 *  <input type="password" id="confirmPassword" name="confirmPassword" #confirmPasswordInput="ngModel"
 *      [(ngModel)]="confirmPassword" [pdmPasswordMatches]="password">
 *  <div *ngIf="confirmPasswordInput.dirty && confirmPasswordInput.invalid">
 *      <small *ngIf="confirmPasswordInput.errors.passwordMatches">
 *          The passwords do not match.
 *      </small>
 *  </div>
 * }
 */
@Directive({
    selector: '[pdmPasswordMatches][formControlName],[pdmPasswordMatches][formControl],[pdmPasswordMatches][ngModel]',
    providers: [
        {provide: NG_VALIDATORS, useExisting: PasswordMatchesDirective, multi: true}
    ]
})
export class PasswordMatchesDirective implements Validator {

    subscribed = false;

    @Input() passwordMatches: FormControl;

    validate(c: FormControl) {
        if (!this.subscribed) {
            // Subscribing to changes to the password control
            this.subscribed = true;
            this.passwordMatches.valueChanges.subscribe(() => {
                c.updateValueAndValidity();
            });
        }

        let value: string = c.value;
        if (value === undefined || value === null || value.length === 0) {
            return null;
        }
        if (this.passwordMatches.value === value) {
            return null;
        }
        return {
            passwordMatches: {
                valid: false
            }
        };
    }

}
