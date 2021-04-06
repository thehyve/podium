/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { BaseRequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { JhiLanguageHelper, AccountService } from '../../shared';
import { SettingsComponent } from './settings.component';
import { MockAccountService } from '../../../../../test/javascript/spec/helpers/mock-account.service';
import { PodiumTestModule } from '../../../../../test/javascript/spec/test.module';


describe('Component Tests', () => {

    describe('SettingsComponent', () => {

        let comp: SettingsComponent;
        let fixture: ComponentFixture<SettingsComponent>;
        let mockAuth: any;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [PodiumTestModule],
                declarations: [SettingsComponent],
                providers: [
                    MockBackend,
                    {
                        provide: AccountService,
                        useClass: MockAccountService
                    },
                    BaseRequestOptions,
                    {
                        provide: JhiLanguageHelper,
                        useValue: null
                    }
                ]
            }).overrideTemplate(SettingsComponent, '')
                .compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(SettingsComponent);
            comp = fixture.componentInstance;
            mockAuth = fixture.debugElement.injector.get(AccountService);
        });

        it('should send the current identity upon save', function () {
            // GIVEN
            let accountValues = {
                firstName: 'John',
                lastName: 'Doe',

                activated: true,
                email: 'john.doe@mail.com',
                langKey: 'en',
                login: 'john'
            };
            mockAuth.setResponse(accountValues);

            // WHEN
            comp.settingsAccount = accountValues;
            comp.save();

            // THEN
            expect(mockAuth.identitySpy).toHaveBeenCalled();
            expect(mockAuth.saveSpy).toHaveBeenCalledWith(accountValues);
            expect(comp.settingsAccount).toEqual(accountValues);
        });

        it('should notify of success upon successful save', function () {
            // GIVEN
            let accountValues = {
                firstName: 'John',
                lastName: 'Doe'
            };
            mockAuth.setResponse(accountValues);

            // WHEN
            comp.save();

            // THEN
            expect(comp.error).toBeNull();
            expect(comp.success).toBe('OK');
        });

        it('should notify of error upon failed save', function () {
            // GIVEN
            mockAuth.saveSpy.and.returnValue(Observable.throw('ERROR'));

            // WHEN
            comp.save();

            // THEN
            expect(comp.error).toEqual('ERROR');
            expect(comp.success).toBeNull();
        });
    });
});
