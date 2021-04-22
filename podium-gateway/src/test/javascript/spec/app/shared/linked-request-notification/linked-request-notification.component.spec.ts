/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import {
    LinkedRequestNotificationComponent
} from '../../../../../../main/webapp/app/shared/linked-request-notification/linked-request-notification.component';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RequestAccessService } from '../../../../../../main/webapp/app/shared/request/request-access.service';
import { RequestService } from '../../../../../../main/webapp/app/shared/request/request.service';
import { Principal } from '../../../../../../main/webapp/app/shared/auth/principal.service';
import { AccountService } from '../../../../../../main/webapp/app/core/auth/account.service';
import { PodiumTestModule } from '../../../test.module';
import { Subscription } from 'rxjs';

describe('LinkedRequestNotificationComponent (templateUrl)', () => {

    let comp: LinkedRequestNotificationComponent;
    let fixture: ComponentFixture<LinkedRequestNotificationComponent>;
    let requestService: RequestService;
    let requestAccessService: RequestAccessService;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {


        TestBed.configureTestingModule({
            imports: [
                PodiumTestModule
            ],
            providers: [
                RequestService,
                RequestAccessService,
                Principal,
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
