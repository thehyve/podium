/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { ComponentFixture, TestBed, async, inject, tick, fakeAsync } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { Http, BaseRequestOptions } from '@angular/http';
import { Renderer, ElementRef } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { TranslateService, TranslateLoader, TranslateParser } from 'ng2-translate';
import { JhiLanguageService } from 'ng-jhipster';
import { MockLanguageService } from '../../../helpers/mock-language.service';
import { LoginModalService } from '../../../../../../main/webapp/app/shared';
import { Register } from '../../../../../../main/webapp/app/account/register/register.service';
import { RegisterComponent } from '../../../../../../main/webapp/app/account/register/register.component';
import { MessageService } from '../../../../../../main/webapp/app/shared/message/message.service';
import { Router } from '@angular/router';
import { Message } from '../../../../../../main/webapp/app/shared/message/message.model';


describe('Component Tests', () => {

    describe('RegisterComponent', () => {
        let fixture: ComponentFixture<RegisterComponent>;
        let comp: RegisterComponent;
        let router = {
            navigate: jasmine.createSpy('navigate')
        };

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                declarations: [RegisterComponent],
                providers: [MockBackend,
                    Register,
                    MessageService,
                    TranslateService,
                    TranslateLoader,
                    TranslateParser,
                    BaseRequestOptions,
                    {
                        provide: Http,
                        useFactory: (backendInstance: MockBackend, defaultOptions: BaseRequestOptions) => {
                            return new Http(backendInstance, defaultOptions);
                        },
                        deps: [MockBackend, BaseRequestOptions]
                    },
                    {
                        provide: JhiLanguageService,
                        useClass: MockLanguageService
                    },

                    {
                        provide: LoginModalService,
                        useValue: null
                    },
                    {
                        provide: Renderer,
                        useValue: null
                    },
                    {
                        provide: Router,
                        useValue: router
                    },
                    {
                        provide: ElementRef,
                        useValue: null
                    }
                ]
            }).overrideComponent(RegisterComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(RegisterComponent);
            comp = fixture.componentInstance;
            comp.ngOnInit();
        });

        it('should update success to OK after creating an account',
            inject([Register, JhiLanguageService, MessageService, Router],
                fakeAsync((service: Register, mockTranslate: MockLanguageService, messageService: MessageService) => {

                    spyOn(service, 'save').and.returnValue(Observable.of({}));
                    spyOn(messageService, 'store');

                    comp.registerAccount.password = comp.confirmPassword = 'password';

                    comp.register();
                    tick();

                    expect(service.save).toHaveBeenCalledWith({
                        password: 'password',
                        specialism: '',
                        langKey: 'en'
                    });

                    expect(comp.success).toEqual(true);
                    expect(comp.registerAccount.langKey).toEqual('en');
                    expect(mockTranslate.getCurrentSpy).toHaveBeenCalled();
                    expect(comp.errorUserExists).toBeNull();
                    expect(comp.errorEmailExists).toBeNull();
                    expect(comp.error).toBeNull();
                })
            )
        );

        it('should notify of user existence upon 400/login already in use',
            inject([Register],
                fakeAsync((service: Register) => {
                    spyOn(service, 'save').and.returnValue(Observable.throw({
                        status: 400,
                        _body: 'login already in use'
                    }));
                    comp.registerAccount.password = comp.confirmPassword = 'password';

                    comp.register();
                    tick();

                    expect(comp.errorUserExists).toEqual('ERROR');
                    expect(comp.errorEmailExists).toBeNull();
                    expect(comp.error).toBeNull();
                })
            )
        );

        it('should notify of email existence upon 400/e-mail address already in use',
            inject([Register],
                fakeAsync((service: Register) => {
                    spyOn(service, 'save').and.returnValue(Observable.throw({
                        status: 400,
                        _body: 'e-mail address already in use'
                    }));
                    comp.registerAccount.password = comp.confirmPassword = 'password';

                    comp.register();
                    tick();

                    expect(comp.errorEmailExists).toEqual('ERROR');
                    expect(comp.errorUserExists).toBeNull();
                    expect(comp.error).toBeNull();
                })
            )
        );

        it('should notify of generic error',
            inject([Register],
                fakeAsync((service: Register) => {
                    spyOn(service, 'save').and.returnValue(Observable.throw({
                        status: 503
                    }));
                    comp.registerAccount.password = comp.confirmPassword = 'password';

                    comp.register();
                    tick();

                    expect(comp.errorUserExists).toBeNull();
                    expect(comp.errorEmailExists).toBeNull();
                    expect(comp.error).toEqual('ERROR');
                })
            )
        );
    });
});
