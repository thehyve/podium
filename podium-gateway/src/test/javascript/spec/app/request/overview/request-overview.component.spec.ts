/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { ComponentFixture, TestBed, async, inject, fakeAsync } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { Http, BaseRequestOptions } from '@angular/http';
import { RequestOverviewComponent } from '../../../../../../main/webapp/app/request/overview/request-overview.component';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiLanguageService, ParseLinks, EventManager } from 'ng-jhipster';
import { MockLanguageService } from '../../../helpers/mock-language.service';
import { Principal } from '../../../../../../main/webapp/app/shared/auth/principal.service';
import { RequestService } from '../../../../../../main/webapp/app/shared/request/request.service';
import { RequestFormService } from '../../../../../../main/webapp/app/request/form/request-form.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { MockRouter } from '../../../helpers/mock-route.service';
import { AccountService } from '../../../../../../main/webapp/app/shared/auth/account.service';
import { Observable } from 'rxjs';
import { PodiumTestModule } from '../../../test.module';

describe('Component Tests', () => {
    describe('Request Overview Component', () => {

        let comp: RequestOverviewComponent;
        let fixture: ComponentFixture<RequestOverviewComponent>;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [PodiumTestModule],
                declarations: [RequestOverviewComponent],
                providers: [
                    RequestService,
                    {
                        provide: Router,  useClass: MockRouter
                    },
                    ParseLinks,
                    MockBackend,
                    RequestFormService,
                    BaseRequestOptions,
                    EventManager,
                    Principal,
                    {
                        provide: NgbModal,
                        useValue: null
                    },
                    {
                        provide: ActivatedRoute,
                        useValue: {
                            data: Observable.from([{
                                'pagingParams': {},
                            }]),
                            snapshot: {
                                url: [{path: '/foo'}],
                                params: {},
                            }
                        },
                    },
                    AccountService,
                ]
            }).overrideTemplate(RequestOverviewComponent, '')
                .compileComponents();
        }));

        // synchronous beforeEach
        beforeEach(() => {
            fixture = TestBed.createComponent(RequestOverviewComponent);
            comp = fixture.componentInstance;
        });

        describe('ngOnInit()', () => {
            beforeEach(() => {
                spyOn(comp, 'registerChangeInRequests');
            });
            it('should load submitted requests and register change in requests', () => {
                comp.ngOnInit();
                expect(comp.registerChangeInRequests).toHaveBeenCalled();
            });
        });

    });
});
