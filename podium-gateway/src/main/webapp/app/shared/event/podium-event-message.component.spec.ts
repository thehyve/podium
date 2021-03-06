/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { ComponentFixture, TestBed, waitForAsync, inject } from '@angular/core/testing';

import { PodiumTestModule } from '../test/test.module';
import { User } from '../user/user.model';
import { RequestOverviewStatusOption } from '../request/request-status/request-status.constants';
import { AccountService } from '../../core/auth/account.service';
import { RequestAccessService } from '../request/request-access.service';
import { RequestService } from '../request/request.service';
import { RequestReviewProcess } from '../request/request-review-process';
import { RequestDetail } from '../request/request-detail';
import { RequestBase } from '../request/request-base';
import { PodiumEvent } from './podium-event';
import { PodiumEventMessageComponent } from './podium-event-message.component';

describe('PodiumEventMessageComponent (templateUrl)', () => {
    let comp: PodiumEventMessageComponent;
    let fixture: ComponentFixture<PodiumEventMessageComponent>;

    // async beforeEach, since we use external templates & styles
    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [PodiumTestModule],
            providers: [
                AccountService,
                RequestService,
                RequestAccessService,
            ],
            declarations: [PodiumEventMessageComponent], // declare the test component
        }).overrideTemplate(PodiumEventMessageComponent, '')
            .compileComponents();
    }));

    let getDummyRequest = (): RequestBase => {
        let revisionEvent = new PodiumEvent();
        let statusChangeEvent = new PodiumEvent();
        let request = new RequestBase();
        request.requestDetail = new RequestDetail();
        request.latestEvent = undefined;

        request.status = RequestOverviewStatusOption.Revision;
        request.requestReview = new RequestReviewProcess();

        revisionEvent.eventDate = new Date();
        revisionEvent.eventType = 'Status_Change';

        revisionEvent.data = {
            sourceStatus: 'Validation',
            targetStatus: 'Revision',
            messageSummary: 'Please clarify your research question',
            messageDescription: 'Your research question requires more detail and update your title.'
        };

        statusChangeEvent.eventDate = new Date();
        statusChangeEvent.eventType = 'Status_Change';

        statusChangeEvent.data = {
            sourceStatus: 'Validation',
            targetStatus: 'Review'
        };

        request.latestEvent = revisionEvent;

        request.requester = new User();
        request.requester.uuid = 'johndoeuuid';

        return request;
    };

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(PodiumEventMessageComponent);
        comp = fixture.componentInstance;
    });

    it('should construct', waitForAsync(
        inject([RequestService, RequestAccessService],
            (requestService, requestAccessService) => {
                expect(requestService).toBeDefined();
                expect(requestAccessService).toBeDefined();
            })
    ));

    describe('ngOnInit', () => {
        let request;

        beforeEach(() => {
            request = getDummyRequest();
        });

        it('should find the last historic message event on init', () => {
            comp.lastEvent = null;
            spyOn(comp, 'findLastHistoricReviewMessageEventForCurrentStatus').and.callThrough();
            comp.request = request;

            fixture.detectChanges(); // initial binding

            expect(comp.findLastHistoricReviewMessageEventForCurrentStatus).toHaveBeenCalled();
            expect(comp.lastEvent).toEqual(request.latestEvent);
        });

    });

    describe('Revision and rejection event types', () => {
        beforeEach(() => {
            comp.lastEvent = null;
            comp.request = getDummyRequest();
            fixture.detectChanges(); // initial binding
        });

        it('should indicate whether the last event is a revision event', () => {
            let isRevisionEvent = comp.isRevisionEvent();
            expect(isRevisionEvent).toBeTruthy();
        });

        it('should indicate whether the last event is a rejection event', () => {
            let isRejectionEvent = comp.isRejectionEvent();
            expect(isRejectionEvent).toBeFalsy();
        });
    });

    describe('Permission for the page', () => {
        beforeEach(() => {
            comp.request = getDummyRequest();
            fixture.detectChanges(); // initial binding
        });

        it('should be able to check whether the current user is the requester', inject([RequestAccessService],
            ((requestAccessService: RequestAccessService) => {
                spyOn(requestAccessService, 'isRequesterOf');
                comp.isRequestOwner();
                expect(requestAccessService.isRequesterOf).toHaveBeenCalledWith(comp.request);
            })
        ));
    });

});
