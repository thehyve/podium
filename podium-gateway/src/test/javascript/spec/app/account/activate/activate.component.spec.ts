/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { TestBed, async, tick, fakeAsync, inject } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { Http, BaseRequestOptions } from '@angular/http';
import {ActivatedRoute, Router} from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { JhiLanguageService } from 'ng-jhipster';
import { MockLanguageService } from '../../../helpers/mock-language.service';

import { MockActivatedRoute, MockRouter } from '../../../helpers/mock-route.service';
import { Activate } from '../../../../../../main/webapp/app/account/activate/activate.service';
import { ActivateComponent } from '../../../../../../main/webapp/app/account/activate/activate.component';


describe('Component Tests', () => {

    describe('ActivateComponent', () => {

        let comp: ActivateComponent;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                declarations: [ActivateComponent],
                providers: [MockBackend,
                    Activate,
                    BaseRequestOptions,
                    {
                        provide: Http,
                        useFactory: (backendInstance: MockBackend, defaultOptions: BaseRequestOptions) => {
                            return new Http(backendInstance, defaultOptions);
                        },
                        deps: [MockBackend, BaseRequestOptions]
                    },
                    {
                        provide: JhiLanguageService,
                        useClass: MockLanguageService
                    },
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({'key': 'ABC123'})
                    },
                    {
                        provide: Router,
                        useValue: new MockRouter()
                    }
                ]
            }).overrideComponent(ActivateComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            let fixture = TestBed.createComponent(ActivateComponent);
            comp = fixture.componentInstance;
        });

        it('calls activate.get with the key from params',
            inject([Activate],
                fakeAsync((service: Activate) => {
                    spyOn(service, 'get').and.returnValue(Observable.of());

                    comp.ngOnInit();
                    tick();

                    expect(service.get).toHaveBeenCalledWith('ABC123');
                })
            )
        );

        it('should set set success to OK upon successful activation',
            inject([Activate],
                fakeAsync((service: Activate) => {
                    spyOn(service, 'get').and.returnValue(Observable.of({}));

                    comp.ngOnInit();
                    tick();

                    expect(comp.error).toBe(null);
                    expect(comp.success).toEqual('OK');
                })
            )
        );

        it('should set set error to ERROR upon activation failure',
            inject([Activate],
                fakeAsync((service: Activate) => {
                    spyOn(service, 'get').and.returnValue(Observable.throw('ERROR'));

                    comp.ngOnInit();
                    tick();

                    expect(comp.error).toBe('ERROR');
                    expect(comp.success).toEqual(null);
                })
            )
        );
    });
});
