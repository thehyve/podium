/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, AfterViewInit, Renderer, ElementRef } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { JhiLanguageService, EventManager } from 'ng-jhipster';

import { LoginService } from '../login/login.service';
import { StateStorageService } from '../auth/state-storage.service';

@Component({
    selector: 'podium-login',
    templateUrl: './login.component.html'
})
export class  PodiumLoginComponent implements OnInit, AfterViewInit {
    authenticationError: boolean;
    userAccountLocked: boolean;
    emailNotVerified: boolean;
    accountNotVerified: boolean;
    password: string;
    rememberMe: boolean;
    username: string;
    credentials: any;

    constructor(
        private eventManager: EventManager,
        private languageService: JhiLanguageService,
        private loginService: LoginService,
        private stateStorageService: StateStorageService,
        private elementRef: ElementRef,
        private renderer: Renderer,
        private router: Router
    ) {
        this.credentials = {};
    }

    ngOnInit() {
        this.languageService.addLocation('login');
    }

    ngAfterViewInit() {
        this.renderer.invokeElementMethod(this.elementRef.nativeElement.querySelector('#username'), 'focus', []);
    }

    cancel () {
        this.credentials = {
            username: null,
            password: null,
            rememberMe: true
        };
        this.authenticationError = false;
        this.userAccountLocked = false;
        this.emailNotVerified = false;
        this.accountNotVerified = false;
    }

    login () {
        this.loginService.login({
            username: this.username,
            password: this.password,
            rememberMe: this.rememberMe
        }).then(() => {
            this.authenticationError = false;
            this.userAccountLocked = false;
            this.emailNotVerified = false;
            this.accountNotVerified = false;

            if (this.router.url === '/register' || this.router.url === '/verify' ||
                this.router.url === '/finishReset' || this.router.url === '/requestReset') {
                this.router.navigate(['']);
            }

            this.eventManager.broadcast({
                name: 'authenticationSuccess',
                content: 'Sending Authentication Success'
            });

            // previousState was set in the authExpiredInterceptor before being redirected to login modal.
            // since login is succesful, go to stored previousState and clear previousState
            let previousState = this.stateStorageService.getPreviousState();
            if (previousState) {
                this.stateStorageService.resetPreviousState();
                this.router.navigate([previousState.name], { queryParams:  previousState.params });
            } else {
                this.router.navigate(['/dashboard']);
            }
        }).catch(err => {
            this.authenticationError = true;
            this.userAccountLocked = false;
            this.emailNotVerified = false;
            this.accountNotVerified = false;
            if (err && err._body) {
                let response =  JSON.parse(err._body);
                switch (response.error_description) {
                    case 'The user account is locked.':
                        this.userAccountLocked = true;
                        break;
                    case 'Email address has not been verified yet.':
                        this.emailNotVerified = true;
                        break;
                    case 'The user account has not been verified yet.':
                        this.accountNotVerified = true;
                        break;
                }
            }
        });
    }

    register () {
        this.router.navigate(['/register']);
    }

    requestResetPassword () {
        this.router.navigate(['/reset', 'request']);
    }
}
