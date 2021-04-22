/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { PasswordResetInit } from './password-reset-init.service';

@Component({
    selector: 'pdm-password-reset-init',
    templateUrl: './password-reset-init.component.html'
})
export class PasswordResetInitComponent implements OnInit, AfterViewInit {
    @ViewChild("emailField") emailField: ElementRef;

    error: string;
    errorEmailNotExists: string;
    resetAccount: any;
    success: string;

    constructor(
        private passwordResetInit: PasswordResetInit,
    ) {

    }

    ngOnInit() {
        this.resetAccount = {};
    }

    ngAfterViewInit() {
        this.emailField.nativeElement.focus();
    }

    requestReset () {

        this.error = null;
        this.errorEmailNotExists = null;

        this.passwordResetInit.save(this.resetAccount.email).subscribe(() => {
            this.success = 'OK';
        }, (response) => {
            this.success = null;
            if (response.status === 400 && response.data === 'e-mail address not registered') {
                this.errorEmailNotExists = 'ERROR';
            } else {
                this.error = 'ERROR';
            }
        });
    }
}
