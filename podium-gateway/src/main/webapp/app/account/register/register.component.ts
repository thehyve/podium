/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, Renderer, ElementRef, AfterViewInit } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { Register } from './register.service';
import { LoginModalService, MessageService } from '../../shared';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { Message } from '../../shared/message/message.model';
import { CompletionType } from '../../layouts/completed/completion-type';

@Component({
    templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit, AfterViewInit {

    confirmPassword: string;
    doNotMatch: string;
    error: string;
    errorEmailExists: string;
    errorUserExists: string;
    registerAccount: any;
    success: boolean;
    successMessage: Message;

    constructor(
        private languageService: JhiLanguageService,
        private translate: TranslateService,
        private registerService: Register,
        private messageService: MessageService,
        private elementRef: ElementRef,
        private renderer: Renderer,
        private router: Router
    ) {
    }

    ngOnInit() {
        this.success = false;
        this.registerAccount = {};
        this.registerAccount.specialism = '';
    }

    ngAfterViewInit() {
        this.renderer.invokeElementMethod(this.elementRef.nativeElement.querySelector('#login'), 'focus', []);
    }

    register() {
        if (this.registerAccount.password !== this.confirmPassword) {
            this.doNotMatch = 'ERROR';
        } else {
            this.doNotMatch = null;
            this.error = null;
            this.errorUserExists = null;
            this.errorEmailExists = null;
            this.languageService.getCurrent().then((key) => {
                this.registerAccount.langKey = key;
                this.registerService.save(this.registerAccount).subscribe(
                    (response) => this.processSuccess(),
                    (error) => this.processError(error)
                );
            });
        }
    }

    gotoLogin() {
        this.router.navigate(['/']);
    }

    private processSuccess() {
        this.success = true;

        // Get i18n success page content
        let successTitle = this.translate.get('register.messages.successTitle');
        let successContent = this.translate.get('register.messages.success');

        Observable.forkJoin(successTitle, successContent).subscribe(
            messages => {
                this.successMessage = new Message(CompletionType.Registration, messages[0], messages[1]);
                this.messageService.store(this.successMessage);
                this.router.navigate(['/completed']);
            });
    }

    private processError(response) {
        this.success = null;
        if (response.status === 400 && response._body === 'login already in use') {
            this.errorUserExists = 'ERROR';
        } else if (response.status === 400 && response._body === 'email address already in use') {
            this.errorEmailExists = 'ERROR';
        } else {
            this.error = 'ERROR';
        }
    }
}
