/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, ViewChild, OnInit, AfterViewInit, ElementRef } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService } from '../core/auth/account.service';
import { EventManager } from '../core/util/event-manager.service';
import { LoginService } from './login.service';

@Component({
    selector: 'pdm-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class PodiumLoginComponent implements OnInit, AfterViewInit {
    @ViewChild('username', { static: false })
    username?: ElementRef;

    authenticationError = false;
    userAccountLocked = false;
    emailNotVerified = false;
    accountNotVerified = false;

    loginForm = this.fb.group({
        username: [null, [Validators.required]],
        password: [null, [Validators.required]],
        rememberMe: [false],
    });

    constructor(
        private accountService: AccountService,
        private eventManager: EventManager,
        private loginService: LoginService,
        private router: Router,
        private fb: FormBuilder
    ) { }

    ngOnInit(): void {
        // if already authenticated then navigate to home page
        this.accountService.identity().subscribe(() => {
            if (this.accountService.isAuthenticated()) {
                this.router.navigate(['']);
            }
        });
    }

    ngAfterViewInit(): void {
        if (this.username) {
            this.username.nativeElement.focus();
        }
    }

    login(): void {
        this.loginService.login({
            username: this.loginForm.get('username')!.value,
            password: this.loginForm.get('password')!.value,
            rememberMe: this.loginForm.get('rememberMe')!.value,
        })
        .subscribe(
            () => {
                this.authenticationError = false;
                if (!this.router.getCurrentNavigation()) {
                    // There were no routing during login (eg from navigationToStoredUrl)
                    this.router.navigate(['']);
                }

                this.eventManager.broadcast({
                    name: 'authenticationSuccess',
                    content: 'Sending Authentication Success'
                });
            },
            (err) => {
                this.authenticationError = true;
                this.userAccountLocked = false;
                this.emailNotVerified = false;
                this.accountNotVerified = false;
                if (err && err.error) {
                    let response = err.error;
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
            }
        );
    }

    register () {
        this.router.navigate(['/register']);
    }

    requestResetPassword () {
        this.router.navigate(['/reset', 'request']);
    }
}
