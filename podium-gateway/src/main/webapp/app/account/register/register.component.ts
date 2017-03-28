/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, Renderer, ElementRef } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';
import { Register } from './register.service';
import { TranslateService } from 'ng2-translate';
import {
    LoginModalService,
    MessageService,
    EmailValidatorDirective,
    SpecialismComponent,
    PasswordValidatorDirective
} from '../../shared';
import { Router } from '@angular/router';
import { Message } from '../../shared/message/message.model';
import { Observable } from 'rxjs';

@Component({
    templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {

    confirmPassword: string;
    doNotMatch: string;
    error: string;
    errorEmailExists: string;
    errorUserExists: string;
    registerAccount: any;
    success: boolean;
    successMessage: Message;
    modalRef: NgbModalRef;

    constructor(private languageService: JhiLanguageService,
                private loginModalService: LoginModalService,
                private messageService: MessageService,
                private registerService: Register,
                private elementRef: ElementRef,
                private renderer: Renderer,
                private translate: TranslateService,
                private router: Router
    ) {
        this.languageService.setLocations(['register']);
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
            window.scrollTo(0, 0);
        } else {
            this.doNotMatch = null;
            this.error = null;
            this.errorUserExists = null;
            this.errorEmailExists = null;
            this.languageService.getCurrent().then(key => {
                this.registerAccount.langKey = key;
                this.registerService.save(this.registerAccount).subscribe(
                    () => this.processSuccess(),
                    (response) => {
                        this.processError(response);
                        window.scrollTo(0, 0);
                    }
                );
            });
        }
    }

    openLogin() {
        this.modalRef = this.loginModalService.open();
    }

    private processSuccess() {
        this.success = true;

        // Get i18n success page content
        let successTitle = this.translate.get('register.messages.successTitle');
        let successContent = this.translate.get('register.messages.success');

        Observable.forkJoin(successTitle, successContent).subscribe(
            messages => {
                this.successMessage = new Message(messages[0], messages[1]);
                this.messageService.store(this.successMessage);
                this.router.navigate(['/completed']);
            });
    }

    private processError(response) {
        this.success = null;
        if (response.status === 400 && response._body === 'login already in use') {
            this.errorUserExists = 'ERROR';
        } else if (response.status === 400 && response._body === 'e-mail address already in use') {
            this.errorEmailExists = 'ERROR';
        } else {
            this.error = 'ERROR';
        }
    }
}
