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
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import {ActivatedRoute, Router} from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';

import { Activate } from './activate.service';
import { LoginModalService } from '../../shared';

@Component({
    selector: 'jhi-activate',
    templateUrl: './activate.component.html'
})
export class ActivateComponent implements OnInit {
    error: string;
    success: string;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private activate: Activate,
        private route: ActivatedRoute,
        private router: Router
    ) {
        this.jhiLanguageService.setLocations(['activate']);
    }

    ngOnInit () {
        this.route.queryParams.subscribe(params => {
            this.activate.get(params['key']).subscribe(() => {
                this.error = null;
                this.success = 'OK';
            }, () => {
                this.success = null;
                this.error = 'ERROR';
            });
        });
    }

    login() {
        this.router.navigate(['/']);
    }
}
