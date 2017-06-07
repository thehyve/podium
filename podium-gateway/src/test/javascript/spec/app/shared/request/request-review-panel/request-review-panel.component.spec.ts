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
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RequestDetail } from '../../../../../../../main/webapp/app/shared/request/request-detail';
import { RequestReviewDecision } from '../../../../../../../main/webapp/app/shared/request/request-review-decision';
import { User } from '../../../../../../../main/webapp/app/shared/user/user.model';
import { RequestReviewFeedback } from '../../../../../../../main/webapp/app/shared/request/request-review-feedback';
import { ReviewRound } from '../../../../../../../main/webapp/app/shared/request/review-round';
import { RequestService } from '../../../../../../../main/webapp/app/shared/request/request.service';
import { MockBackend } from '@angular/http/testing';
import { BaseRequestOptions, Http } from '@angular/http';
import { PodiumEventMessage } from '../../../../../../../main/webapp/app/shared/event/podium-event-message';

describe('RequestReviewPanelComponent (templateUrl)', () => {

    let comp: RequestReviewPanelComponent;
    let fixture: ComponentFixture<RequestReviewPanelComponent>;
    let de: DebugElement;
    let el: HTMLElement;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                MockBackend,
                BaseRequestOptions,
                RequestService,
                {
                    provide: Http,
                    useFactory: (backendInstance: MockBackend, defaultOptions: BaseRequestOptions) => {
                        return new Http(backendInstance, defaultOptions);
                    },
                    deps: [MockBackend, BaseRequestOptions]
                }
            ],
            imports: [FormsModule],
            declarations: [RequestReviewPanelComponent], // declare the test component
        }).overrideComponent(RequestReviewPanelComponent, {
            set: {
                template: ''
            }
        }).compileComponents();

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
        });

        it('should get last review feedback on initialisation', () => {
            comp.reviewRounds = [];
            fixture.detectChanges(); // initial binding
            comp.ngOnInit();
            expect(comp.lastReviewFeedback).toBe(undefined);
        });

        it('should get last review feedback on initialisation', () => {
            comp.reviewRounds = [reviewRound1, reviewRound2];
            comp.ngOnInit();
            expect(comp.lastReviewFeedback.length).toBe(2);
        });
    });

    describe('toggleAdviseStyle', () => {

        it('should give default style when no advice found', () => {
            let _adviseStyle = comp.toggleAdviseStyle(undefined);
            expect(_adviseStyle).toEqual('tag-default');
        });

        it('should give success style when advise is approved', () => {
            let _adviseStyle = comp.toggleAdviseStyle(RequestReviewDecision.Approved);
            expect(_adviseStyle).toEqual('tag-success');
        });

    });

});
