/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';

import { EventManager } from '../../core/util/event-manager.service';
import { AccountService } from '../../core/auth/account.service';
import { RequestFormService } from '../form/request-form.service';
import { PodiumTestModule } from '../../shared/test/test.module';
import { RequestService } from '../../shared/request/request.service';
import { RequestOverviewComponent } from './request-overview.component';

describe('Component Tests', () => {
    describe('Request Overview Component', () => {

        let comp: RequestOverviewComponent;
        let fixture: ComponentFixture<RequestOverviewComponent>;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [PodiumTestModule],
                declarations: [RequestOverviewComponent],
                providers: [
                    RequestService,
                    RequestFormService,
                    EventManager,
                    {
                        provide: NgbModal,
                        useValue: null
                    },
                    {
                        provide: ActivatedRoute,
                        useValue: {
                            data: of([{
                                'pagingParams': {},
                            }]),
                            snapshot: {
                                url: [{path: 'my-requests'}],
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
                spyOn(comp, 'registerChanges');
            });
            it('should load submitted requests and register change in requests', () => {
                comp.ngOnInit();
                comp.ngAfterViewInit();
                expect(comp.registerChanges).toHaveBeenCalled();
            });
        });

    });
});
