/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, Renderer, ElementRef } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { PasswordResetFinish } from './password-reset-finish.service';

@Component({
    selector: 'jhi-password-reset-finish',
    templateUrl: './password-reset-finish.component.html'
})
export class PasswordResetFinishComponent implements OnInit {
    confirmPassword: string;
    error: string;
    keyMissing: boolean;
    resetAccount: any;
    success: string;
    modalRef: NgbModalRef;
    key: string;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private passwordResetFinish: PasswordResetFinish,
        private route: ActivatedRoute,
        private elementRef: ElementRef,
        private renderer: Renderer,
        private router: Router) {
        this.jhiLanguageService.setLocations(['reset']);
    }

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            this.key = params['key'];
        });
        this.resetAccount = {};
        this.keyMissing = !this.key;
    }

    ngAfterViewInit() {
        if (this.elementRef.nativeElement.querySelector('#password') != null) {
            this.renderer.invokeElementMethod(this.elementRef.nativeElement.querySelector('#password'), 'focus', []);
        }
    }

    finishReset() {
        this.error = null;
        this.passwordResetFinish.save({ key: this.key, newPassword: this.resetAccount.password }).subscribe(() => {
            this.success = 'OK';
        }, () => {
            this.success = null;
            this.error = 'ERROR';
        });
    }

    login() {
        this.router.navigate(['/']);
    }
}
