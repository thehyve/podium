/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RequestReviewPanelComponent }
    from '../../../../../../../main/webapp/app/shared/request/request-review-panel/request-review-panel.component';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RequestDetail } from '../../../../../../../main/webapp/app/shared/request/request-detail';
import { RequestReviewDecision } from '../../../../../../../main/webapp/app/shared/request/request-review-decision';
import { User } from '../../../../../../../main/webapp/app/shared/user/user.model';
import { RequestReviewFeedback } from '../../../../../../../main/webapp/app/shared/request/request-review-feedback';
import { ReviewRound } from '../../../../../../../main/webapp/app/shared/request/review-round';
import { RequestService } from '../../../../../../../main/webapp/app/shared/request/request.service';
import { MockBackend } from '@angular/http/testing';
import { BaseRequestOptions } from '@angular/http';
import { PodiumEventMessage } from '../../../../../../../main/webapp/app/shared/event/podium-event-message';
import { Principal } from '../../../../../../../main/webapp/app/shared/auth/principal.service';
import { AccountService } from '../../../../../../../main/webapp/app/core/auth/account.service';
import { RequestAccessService } from '../../../../../../../main/webapp/app/shared/request/request-access.service';
import { PodiumTestModule } from '../../../../test.module';

describe('RequestReviewPanelComponent (templateUrl)', () => {

    let comp: RequestReviewPanelComponent;
    let fixture: ComponentFixture<RequestReviewPanelComponent>;

    // async beforeEach, since we use external templates & styles
    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                FormsModule,
                PodiumTestModule
            ],
            providers: [
                MockBackend,
                BaseRequestOptions,
                RequestService,
                Principal,
                AccountService,
                RequestAccessService
            ],
            declarations: [RequestReviewPanelComponent], // declare the test component
        }).overrideTemplate(RequestReviewPanelComponent, '')
            .compileComponents();
    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(RequestReviewPanelComponent);
        comp = fixture.componentInstance;
    });


    describe('ngOnInit', () => {

        let reviewRound1 = new ReviewRound();
        let reviewRound2 = new ReviewRound();
        let reviewFeedback1 = new RequestReviewFeedback();
        let reviewFeedback2 = new RequestReviewFeedback();
        let reviewFeedback3 = new RequestReviewFeedback();
        let msg1 = new PodiumEventMessage();
        let msg2 = new PodiumEventMessage();
        let msg3 = new PodiumEventMessage();
        let dummyUser = new User();

        beforeEach(() => {
            dummyUser.uuid = 'dumdum01';
            dummyUser.firstName = 'Foo';
            dummyUser.lastName = 'Bar';

            msg1.summary = 'summary1';
            msg1.description = 'desc1';
            msg2.summary = 'summary2';
            msg2.description = 'desc2';
            msg3.summary = 'summary3';
            msg3.description = 'desc3';

            reviewFeedback1.id = '01';
            reviewFeedback1.reviewer = dummyUser;
            reviewFeedback1.advice = RequestReviewDecision.Approved;
            reviewFeedback1.date = new Date();
            reviewFeedback1.message  = msg1;


            reviewFeedback2.id = '02';
            reviewFeedback2.reviewer = dummyUser;
            reviewFeedback2.advice = RequestReviewDecision.Rejected;
            reviewFeedback2.date = new Date();
            reviewFeedback1.message  = msg2;

            reviewFeedback3.id = '03';
            reviewFeedback3.reviewer = dummyUser;
            reviewFeedback3.advice = RequestReviewDecision.None;
            reviewFeedback3.date = new Date();
            reviewFeedback1.message  = msg3;

            reviewRound1.id = 'round-1';
            reviewRound1.initiatedBy = dummyUser;
            reviewRound1.requestDetail = new RequestDetail();
            reviewRound1.startDate = new Date('2017-05-01T00:00:00');
            reviewRound1.endDate = new Date('2017-05-02T00:00:00');
            reviewRound1.reviewFeedback = [
                reviewFeedback1
            ];

            reviewRound2.id = 'round-2';
            reviewRound2.initiatedBy = dummyUser;
            reviewRound2.requestDetail = new RequestDetail();
            reviewRound2.startDate = new Date('2017-05-10T00:00:00');
            reviewRound2.endDate = null;
            reviewRound2.reviewFeedback = [
                reviewFeedback2, reviewFeedback3
            ];
            comp.request = {};
        });

        it('should get last review feedback on initialisation', () => {
            comp.request.reviewRound = undefined;
            fixture.detectChanges(); // initial binding
            comp.ngOnInit();
            expect(comp.lastReviewFeedback).toBe(undefined);
        });

        it('should get review feedback on initialisation', () => {
            comp.request.reviewRound = reviewRound2;
            comp.ngOnInit();
            expect(comp.lastReviewFeedback.length).toBe(2);
        });
    });

    describe('toggleAdviseStyle', () => {

        it('should give default style when no advice found', () => {
            let _adviseStyle = comp.toggleAdviseStyle(undefined);
            expect(_adviseStyle).toEqual('badge-default');
        });

        it('should give success style when advise is approved', () => {
            let _adviseStyle = comp.toggleAdviseStyle(RequestReviewDecision.Approved);
            expect(_adviseStyle).toEqual('badge-success');
        });

    });

});
