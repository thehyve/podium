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
import { DebugElement, EventEmitter } from '@angular/core';
import { PodiumEventMessageComponent } from '../../../../../../main/webapp/app/shared/event/podium-event-message.component';
import { PodiumEvent } from '../../../../../../main/webapp/app/shared/event/podium-event';
import { RequestBase } from '../../../../../../main/webapp/app/shared/request/request-base';
import { RequestDetail } from '../../../../../../main/webapp/app/shared/request/request-detail';
import { RequestService } from '../../../../../../main/webapp/app/shared/request/request.service';
import { RequestAccessService } from '../../../../../../main/webapp/app/shared/request/request-access.service';
import { Http, BaseRequestOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { MockPrincipal } from '../../../helpers/mock-principal.service';
import { Principal } from '../../../../../../main/webapp/app/shared/auth/principal.service';

describe('PodiumEventMessageComponent (templateUrl)', () => {

    let comp: PodiumEventMessageComponent;
    let fixture: ComponentFixture<PodiumEventMessageComponent>;
    let de: DebugElement;
    let el: HTMLElement;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                BaseRequestOptions,
                MockBackend,
                RequestService,
                RequestAccessService,
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
            declarations: [PodiumEventMessageComponent], // declare the test component
        }).overrideComponent(PodiumEventMessageComponent, {
            set: {
                template: ''
            }
        }).compileComponents();

    }));

    let getDummyRequest = (): RequestBase => {
        let revisionEvent = new PodiumEvent();
        let statusChangeEvent = new PodiumEvent();
        let request = new RequestBase();
        request.requestDetail = new RequestDetail();
        request.historicEvents = [];

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

        request.historicEvents.push(statusChangeEvent);
        request.historicEvents.push(revisionEvent);

        return request;
    };

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(PodiumEventMessageComponent);
        comp = fixture.componentInstance;
    });

    it('should construct', async(
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
            spyOn(comp, 'findLastHistoricMessageEventForCurrentStatus').and.callThrough();
            comp.request = request;

            fixture.detectChanges(); // initial binding

            expect(comp.findLastHistoricMessageEventForCurrentStatus).toHaveBeenCalled();
            expect(comp.lastEvent).toEqual(request.historicEvents[1]);
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

});
