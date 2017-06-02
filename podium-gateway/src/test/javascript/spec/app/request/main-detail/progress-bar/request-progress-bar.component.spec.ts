/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { ComponentFixture, TestBed, async, inject } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { Http, BaseRequestOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { TranslateService, TranslateLoader, TranslateParser } from 'ng2-translate';
import { RequestProgressBarComponent } from '../../../../../../../main/webapp/app/request/main-detail/progress-bar/request-progress-bar.component';
import { JhiLanguageService } from 'ng-jhipster';
import { Principal } from '../../../../../../../main/webapp/app/shared/auth/principal.service';
import { MockLanguageService } from '../../../../helpers/mock-language.service';
import { MockPrincipal } from '../../../../helpers/mock-principal.service';
import { RequestBase } from '../../../../../../../main/webapp/app/shared/request/request-base';
import { RequestReviewProcess } from '../../../../../../../main/webapp/app/shared/request/request-review-process';
import { RequestService } from '../../../../../../../main/webapp/app/shared/request/request.service';
import { RequestAccessService } from '../../../../../../../main/webapp/app/shared/request/request-access.service';
import {
    RequestReviewStatusOptions,
    RequestStatusOptions
} from '../../../../../../../main/webapp/app/shared/request/request-status/request-status.constants';
import { RequestReviewDecision } from '../../../../../../../main/webapp/app/shared/request/request-review-decision';

describe('RequestProgressBarComponent', () => {
    let comp: RequestProgressBarComponent;
    let fixture: ComponentFixture<RequestProgressBarComponent>;
    let de: DebugElement;
    let el: HTMLElement;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                BaseRequestOptions,
                MockBackend,
                JhiLanguageService,
                TranslateService,
                TranslateLoader,
                TranslateParser,
                RequestService,
                RequestAccessService,
                {
                    provide: JhiLanguageService,
                    useClass: MockLanguageService
                },
                {
                    provide: Principal,
                    useClass: MockPrincipal
                },
                {
                    provide: Http,
                    useFactory: (backendInstance: MockBackend, defaultOptions: BaseRequestOptions) => {
                        return new Http(backendInstance, defaultOptions);
                    },
                    deps: [MockBackend, BaseRequestOptions]
                }
            ],
            declarations: [RequestProgressBarComponent], // declare the test component
        }).overrideComponent(RequestProgressBarComponent, {
            set: {
                template: ''
            }
        }).compileComponents();

    }));

    let getDummyRequestWithStatus = (
        status: RequestStatusOptions = RequestStatusOptions.None,
        reviewStatus?: RequestReviewStatusOptions
    ): RequestBase => {
        // Only interested in the statuses of the request and its processes
        let request = new RequestBase();
        request.status = status;

        if (reviewStatus != null) {
            request.requestReview = new RequestReviewProcess();
            request.requestReview.status = reviewStatus;
        }

        return request;
    };

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(RequestProgressBarComponent);
        comp = fixture.componentInstance;
    });

    it('should construct', async(
        inject([JhiLanguageService, RequestService, RequestAccessService],
            (jhiLanguageService, requestService, requestAccessService) => {
                expect(jhiLanguageService).toBeDefined();
                expect(requestService).toBeDefined();
                expect(requestAccessService).toBeDefined();
                expect(comp.requestSubscription).toBeDefined();
                expect(comp.requestStatusOptions).toBeDefined();
                expect(comp.requestStatusMap).toBeDefined();
                expect(comp.requestReviewStatusOptions).toBeDefined();
                expect(comp.requestReviewStatusMap).toBeDefined();
            })
    ));

    describe('state indicators', () => {
        // isActive
        it('should be able to indicate that a status is currently active', () => {
            // Validation is the second option (order index 2) in the progress bar
            let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Validation);
            let isActive = comp.isActive(request, 2);
            expect(isActive).toBeTruthy();
        });

        it('should be able to indicate that a status is currently not active', () => {
            // Review is the third option (order index 3) in the progress bar
            let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Review);
            let isActive = comp.isActive(request, 2);
            expect(isActive).toBeFalsy();
        });

        // isCompleted
        it('should be able to indicate that a status step has been completed', () => {
            // Review is the third option (order index 3) in the progress bar
            let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Review);
            // The first item in the progress bar should have been completed.
            let isCompleted = comp.isCompleted(request, 1);
            expect(isCompleted).toBeTruthy();
        });

        it('should be able to indicate that a status step has not been completed', () => {
            // Review is the third option (order index 3) in the progress bar
            let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Review);
            // The fifth item (delivery) in the progress bar should not have been completed.
            let isCompleted = comp.isCompleted(request, 5);
            expect(isCompleted).toBeFalsy();
        });

        // isTerminatedReview
        it('should be able to indicate that a request has been terminated', () => {
            let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Closed);
            // The review decision should be Rejected in order to be a valid terminated request
            request.requestReview.decision = RequestReviewDecision.Rejected;
            let terminatedRequest = comp.isTerminatedReview(request);
            expect(terminatedRequest).toBeTruthy();
        });

        it('should be able to indicate that a closed request has not been terminated', () => {
            let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Closed);
            // The review decision should be Rejected in order to be a valid terminated request
            request.requestReview.decision = RequestReviewDecision.Approved;
            let approvedRequest = comp.isTerminatedReview(request);
            expect(approvedRequest).toBeFalsy();
        });

        // isRevisionStatus
        it('should be able to indicate that a request is in Revision', inject([RequestAccessService],
            ((requestAccessService: RequestAccessService) => {
                let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Revision);
                spyOn(requestAccessService, 'isRequestReviewStatus').and.returnValue(true);

                let revisionRequest = comp.isRevisionStatus(request);

                expect(requestAccessService.isRequestReviewStatus)
                    .toHaveBeenCalledWith(request, RequestReviewStatusOptions.Revision);
                expect(revisionRequest).toBeTruthy();
            })
        ));

        it('should be able to indicate that a request is not in Revision', inject([RequestAccessService],
            ((requestAccessService: RequestAccessService) => {
                let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Review);
                spyOn(requestAccessService, 'isRequestReviewStatus').and.returnValue(false);

                let reviewRequest = comp.isRevisionStatus(request);

                expect(requestAccessService.isRequestReviewStatus)
                    .toHaveBeenCalledWith(request, RequestReviewStatusOptions.Revision);
                expect(reviewRequest).toBeFalsy();
            })
        ));

    });

    describe('helper functions', () => {
        // getRequestStatusOrder
        it('should correctly indicate the request status order', () => {
            // Expect progress order 3 to be returned
            let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Review);
            let progressStatusOrder = comp.getRequestStatusOrder(request);
            expect(progressStatusOrder).toBe(3);
        });

        it('should return 0 when the request status is not mapped', () => {
            // Expect progress order 0 to be returned
            let request = getDummyRequestWithStatus(RequestStatusOptions.Closed);
            let progressStatusOrder = comp.getRequestStatusOrder(request);
            expect(progressStatusOrder).toBe(0);
        });

        it('should correctly indicate the request review status order', () => {
            // Expect progress order 2 to be returned for Validation
            let request = getDummyRequestWithStatus(RequestStatusOptions.Review, RequestReviewStatusOptions.Validation);
            let progressStatusOrder = comp.getRequestStatusOrder(request);
            expect(progressStatusOrder).toBe(2);
        });
    });
});
