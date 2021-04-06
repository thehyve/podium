/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Verify } from './verify.service';

@Component({
    selector: 'pdm-verify',
    templateUrl: './verify.component.html'
})
export class VerifyComponent implements OnInit {
    error: string;
    errorMsg: string;
    success: string;
    renewSuccess: string;
    renewError: string;
    verifyKey: string;

    constructor(
        private verify: Verify,
        private route: ActivatedRoute,
        private router: Router
    ) {

    }

    ngOnInit () {
        this.route.queryParams.subscribe(params => {
            this.verifyKey = params['key'];
            this.verify.get(params['key']).subscribe(() => {
                this.error = null;
                this.success = 'OK';
            }, (error) => {
                this.success = null;
                this.error = 'ERROR';
                this.errorMsg = error._body;
            });
        });
    }

    login() {
        this.router.navigate(['/']);
    }

    requestNewVerificationKey() {
        this.verify.renew(this.verifyKey).subscribe(() => {
            this.error = null;
            this.renewSuccess = 'OK';
        }, (error) => {
            this.success = null;
            this.renewError = 'ERROR';
        });
    }
}
