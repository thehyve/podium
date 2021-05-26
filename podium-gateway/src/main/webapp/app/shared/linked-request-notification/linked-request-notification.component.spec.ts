/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { Subscription } from 'rxjs';

import { PodiumTestModule } from '../test/test.module';
import {
    LinkedRequestNotificationComponent
} from './linked-request-notification.component';
import { AccountService } from '../../core/auth/account.service';
import { RequestService } from '../request/request.service';
import { RequestAccessService } from '../request/request-access.service';

describe('LinkedRequestNotificationComponent (templateUrl)', () => {

    let comp: LinkedRequestNotificationComponent;
    let fixture: ComponentFixture<LinkedRequestNotificationComponent>;
    let requestService: RequestService;
    let requestAccessService: RequestAccessService;

    // async beforeEach, since we use external templates & styles
    beforeEach(waitForAsync(() => {


        TestBed.configureTestingModule({
            imports: [
                PodiumTestModule
            ],
            providers: [
                RequestService,
                RequestAccessService,
                AccountService
            ],
            declarations: [LinkedRequestNotificationComponent], // declare the test component
        })
            .overrideTemplate(LinkedRequestNotificationComponent, '')
            .compileComponents();

    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(LinkedRequestNotificationComponent);
        comp = fixture.componentInstance; // component test instance
        requestService = fixture.debugElement.injector.get(RequestService);
        requestAccessService = fixture.debugElement.injector.get(RequestAccessService);
    });

    describe('OnInit', () => {
        it('should subscribe to request change', () => {
            spyOn(requestService.onRequestUpdate, 'subscribe');
            comp.ngOnInit();
            expect(requestService.onRequestUpdate.subscribe).toHaveBeenCalled();
        });
    });

    describe('OnDestroy', () => {
        it('should unsubscribe to request change', () => {
            comp.requestSubscription = new Subscription();
            spyOn(comp.requestSubscription, 'unsubscribe');
            comp.ngOnDestroy();
            expect(comp.requestSubscription.unsubscribe).toHaveBeenCalled();
        });
    });

});
