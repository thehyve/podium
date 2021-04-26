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
import { BaseRequestOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RequestProgressBarComponent }
    from '../../../../../../../main/webapp/app/request/main-detail/progress-bar/request-progress-bar.component';
import { JhiLanguageService } from 'ng-jhipster';
import { RequestBase } from '../../../../../../../main/webapp/app/shared/request/request-base';
import { RequestReviewProcess } from '../../../../../../../main/webapp/app/shared/request/request-review-process';
import { RequestService } from '../../../../../../../main/webapp/app/shared/request/request.service';
import { RequestAccessService } from '../../../../../../../main/webapp/app/shared/request/request-access.service';
import {
    RequestOverviewStatusOption,
} from '../../../../../../../main/webapp/app/shared/request/request-status/request-status.constants';
import { PodiumTestModule } from '../../../../test.module';

describe('RequestProgressBarComponent', () => {
    let comp: RequestProgressBarComponent;
    let fixture: ComponentFixture<RequestProgressBarComponent>;

    // async beforeEach, since we use external templates & styles
    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [PodiumTestModule],
            providers: [
                BaseRequestOptions,
                MockBackend,
                RequestService,
                RequestAccessService
            ],
            declarations: [RequestProgressBarComponent], // declare the test component
        }).overrideTemplate(RequestProgressBarComponent, '')
            .compileComponents();
    }));

    let getDummyRequestWithStatus = (
        status: RequestOverviewStatusOption = RequestOverviewStatusOption.None
    ): RequestBase => {
        // Only interested in the statuses of the request and its processes
        let request = new RequestBase();
        request.status = status;

        request.requestReview = new RequestReviewProcess();
        return request;
    };

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(RequestProgressBarComponent);
        comp = fixture.componentInstance;
    });

    it('should construct', waitForAsync(
        inject([JhiLanguageService, RequestService, RequestAccessService],
            (jhiLanguageService, requestService, requestAccessService) => {
                expect(jhiLanguageService).toBeDefined();
                expect(requestService).toBeDefined();
                expect(requestAccessService).toBeDefined();
                expect(comp.requestSubscription).toBeDefined();
                expect(comp.requestStatusOptions).toBeDefined();
            })
    ));

    describe('state indicators', () => {
        // isActive
        it('should be able to indicate that a status is currently active', () => {
            // Validation is the second option (order index 2) in the progress bar
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Validation);
            let isActive = comp.isActive(request, 2);
            expect(isActive).toBeTruthy();
        });

        it('should be able to indicate that a status is currently not active', () => {
            // Review is the third option (order index 3) in the progress bar
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Review);
            let isActive = comp.isActive(request, 2);
            expect(isActive).toBeFalsy();
        });

        // isCompleted
        it('should be able to indicate that a status step has been completed', () => {
            // Review is the third option (order index 3) in the progress bar
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Review);
            // The first item in the progress bar should have been completed.
            let isCompleted = comp.isCompleted(request, 1);
            expect(isCompleted).toBeTruthy();
        });

        it('should be able to indicate that a status step has not been completed', () => {
            // Review is the third option (order index 3) in the progress bar
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Review);
            // The fifth item (delivery) in the progress bar should not have been completed.
            let isCompleted = comp.isCompleted(request, 5);
            expect(isCompleted).toBeFalsy();
        });

        // isClosed
        it('should be able to indicate that a request has been terminated after Approval and highlight the current step', () => {
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Closed_Approved);
            let approvedClosedRequest = comp.isClosed(request, 4);
            expect(approvedClosedRequest).toBeTruthy();
        });

        // isRevisionStatus
        it('should be able to indicate that a request is in Revision', () => {
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Revision);
            let revisionRequest = comp.isRevisionStatus(request);
            expect(revisionRequest).toBeTruthy();
        });

        it('should be able to indicate that a request is not in Revision', () => {
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Review);
            let reviewRequest = comp.isRevisionStatus(request);
            expect(reviewRequest).toBeFalsy();
        });

    });

    describe('helper functions', () => {
        // getRequestStatusOrder
        it('should correctly indicate the request status order', () => {
            // Expect progress order 3 to be returned
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Review);
            let progressStatusOrder = comp.getRequestStatusOrder(request);
            expect(progressStatusOrder).toBe(3);
        });

        it('should return 0 when the request status is not mapped', () => {
            // Expect progress order 0 to be returned
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.None);
            let progressStatusOrder = comp.getRequestStatusOrder(request);
            expect(progressStatusOrder).toBe(0);
        });

        it('should correctly indicate the request review status order', () => {
            // Expect progress order 2 to be returned for Validation
            let request = getDummyRequestWithStatus(RequestOverviewStatusOption.Validation);
            let progressStatusOrder = comp.getRequestStatusOrder(request);
            expect(progressStatusOrder).toBe(2);
        });
    });
});
