/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, ElementRef, AfterViewInit, ViewChild } from '@angular/core';
import { Register } from './register.service';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Message } from '../../shared/message/message.model';
import { MessageService } from '../../shared/message/message.service';
import { CompletionType } from '../../shared/completed/completion-type';

@Component({
    templateUrl: './register.component.html',
})
export class RegisterComponent implements AfterViewInit {
    @ViewChild('login', { static: false })
    login?: ElementRef;

    doNotMatch: string;
    error: string;
    errorUserExists: string;
    success = false;
    successMessage: Message;
    specialism = '';

    registerForm = this.fb.group({
        login: [
            '',
            [
                Validators.required,
                Validators.minLength(1),
                Validators.maxLength(50),
                Validators.pattern('^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$'),
            ],
        ],
        email: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email]],
        password: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
        confirmPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
        telephone: ['', [Validators.required, Validators.maxLength(15), Validators.pattern('^[0-9]+$')]],
        institute: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(150)]],
        department: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(150)]],
        jobTitle: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(150)]],
    });

    constructor(
        private translate: TranslateService,
        private registerService: Register,
        private messageService: MessageService,
        private fb: FormBuilder,
        private router: Router
    ) {
    }

    ngAfterViewInit(): void {
        if (this.login) {
            this.login.nativeElement.focus();
        }
    }

    register(): void {
        let password = this.registerForm.get(['password'])!.value;
        if (password !== this.registerForm.get(['confirmPassword'])!.value) {
            this.doNotMatch = 'ERROR';
        } else {
            this.doNotMatch = null;
            this.error = null;
            this.errorUserExists = null;
        
            let userData = {
                password,
                langKey: this.translate.currentLang || 'en',
                login: this.registerForm.get(['login'])!.value,
                email: this.registerForm.get(['email'])!.value,
                telephone: this.registerForm.get(['telephone'])!.value,
                institute: this.registerForm.get(['institute'])!.value,
                department: this.registerForm.get(['department'])!.value,
                jobTitle: this.registerForm.get(['jobTitle'])!.value,
                specialism: this.specialism,
            };
            this.registerService.save(userData).subscribe(
                () => this.processSuccess(),
                (error) => this.processError(error)
            );
        }
    }

    public processSuccess() {
        this.success = true;

        // Get i18n success page content
        let successTitle = this.translate.get('register.messages.successTitle');
        let successContent = this.translate.get('register.messages.success');

        forkJoin([successTitle, successContent]).subscribe(
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
        } else {
            this.error = 'ERROR';
        }
    }

}
