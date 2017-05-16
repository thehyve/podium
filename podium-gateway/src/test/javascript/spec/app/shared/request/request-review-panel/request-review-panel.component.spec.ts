/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { RequestReviewPanelComponent } from '../../../../../../../main/webapp/app/shared/request/request-review-panel/request-review-panel.component';
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RequestDetail } from '../../../../../../../main/webapp/app/shared/request/request-detail';
import { RequestReviewDecision } from '../../../../../../../main/webapp/app/shared/request/request-review-decision';
import { User } from '../../../../../../../main/webapp/app/shared/user/user.model';
import { RequestReviewFeedback } from '../../../../../../../main/webapp/app/shared/request/request-review-feedback';
import { ReviewRound } from '../../../../../../../main/webapp/app/shared/request/review-round';

describe('RequestReviewPanelComponent (templateUrl)', () => {

    let comp: RequestReviewPanelComponent;
    let fixture: ComponentFixture<RequestReviewPanelComponent>;
    let de: DebugElement;
    let el: HTMLElement;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [],
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
        comp = fixture.componentInstance; // OrganisationSelectorComponent test instance

        // de = fixture.debugElement.query(By.css('h1'));
        // el = de.nativeElement;
    });

    describe('ngOnInit', () => {

        let reviewRound1 = new ReviewRound();
        let reviewRound2 = new ReviewRound();
        let reviewFeedback1 = new RequestReviewFeedback();
        let reviewFeedback2 = new RequestReviewFeedback();
        let reviewFeedback3 = new RequestReviewFeedback();
        let dummyUser = new User();


        beforeEach(() => {
            dummyUser.uuid = 'dumdum01';
            dummyUser.firstName = 'Foo';
            dummyUser.lastName = 'Bar';

            reviewFeedback1.id = '01';
            reviewFeedback1.reviewer = dummyUser;
            reviewFeedback1.advice = RequestReviewDecision.Approved;
            reviewFeedback1.date = new Date();
            reviewFeedback1.description = 'This is description';
            reviewFeedback1.summary = 'This is summary';

            reviewFeedback2.id = '02';
            reviewFeedback2.reviewer = dummyUser;
            reviewFeedback2.advice = RequestReviewDecision.Rejected;
            reviewFeedback2.date = new Date();
            reviewFeedback2.description = 'This is description';
            reviewFeedback2.summary = 'This is summary';

            reviewFeedback3.id = '03';
            reviewFeedback3.reviewer = dummyUser;
            reviewFeedback3.advice = RequestReviewDecision.None;
            reviewFeedback3.date = new Date();
            reviewFeedback3.description = '';
            reviewFeedback3.summary = '';

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

            comp.reviewRounds = [reviewRound1, reviewRound2]
        });

        it('should get last review feedback on initialisation', () => {
            comp.ngOnInit();
            expect(comp.lastReviewFeedback.length).toBe(2);
        });
    });

});
