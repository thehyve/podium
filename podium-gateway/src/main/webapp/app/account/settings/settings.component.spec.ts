/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { PodiumTestModule } from '../../shared/test/test.module';

import { AccountService } from '../../core/auth/account.service';
import { SettingsComponent } from './settings.component';

describe('Component Tests', () => {

    describe('SettingsComponent', () => {

        let comp: SettingsComponent;
        let fixture: ComponentFixture<SettingsComponent>;
        let mockAuth: any;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [PodiumTestModule],
                declarations: [SettingsComponent],
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
            mockAuth.mockIdentity(of(accountValues));
            mockAuth.mockSave(of(undefined));

            // WHEN
            comp.settingsAccount = accountValues;
            comp.save();

            // THEN
            expect(mockAuth.identity).toHaveBeenCalled();
            expect(mockAuth.save).toHaveBeenCalledWith(accountValues);
            expect(comp.settingsAccount).toEqual(accountValues);
        });

        it('should notify of success upon successful save', function () {
            // GIVEN
            let accountValues = {
                firstName: 'John',
                lastName: 'Doe'
            };
            mockAuth.mockIdentity(of(accountValues));
            mockAuth.mockSave(of(undefined));

            // WHEN
            comp.save();

            // THEN
            expect(comp.error).toBeNull();
            expect(comp.success).toBe('OK');
        });

        it('should notify of error upon failed save', function () {
            // GIVEN
            mockAuth.mockSave(throwError('ERROR'));

            // WHEN
            comp.save();

            // THEN
            expect(comp.error).toEqual('ERROR');
            expect(comp.success).toBeNull();
        });
    });
});
