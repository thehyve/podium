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
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { JhiLanguageService } from 'ng-jhipster';
import { MockLanguageService } from '../../../helpers/mock-language.service';
import { MockActivatedRoute, MockRouter } from '../../../helpers/mock-route.service';
import { Verify } from '../../../../../../main/webapp/app/account/verify/verify.service';
import { VerifyComponent } from '../../../../../../main/webapp/app/account/verify/verify.component';


describe('Component Tests', () => {

    describe('VerifyComponent', () => {

        let comp: VerifyComponent;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                declarations: [VerifyComponent],
                providers: [MockBackend,
                    Verify,
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
                        useValue: new MockActivatedRoute({ 'key': 'ABC123' })
                    },
                    {
                        provide: Router,
                        useValue: new MockRouter()
                    }
                ]
            }).overrideComponent(VerifyComponent, {
                set: {
                    template: ''
                }
            }).compileComponents();
        }));

        beforeEach(() => {
            let fixture = TestBed.createComponent(VerifyComponent);
            comp = fixture.componentInstance;
        });

        it('calls activate.get with the key from params',
            inject([Verify],
                fakeAsync((service: Verify) => {
                    spyOn(service, 'get').and.returnValue(Observable.of());

                    comp.ngOnInit();
                    tick();

                    expect(service.get).toHaveBeenCalledWith('ABC123');
                })
            )
        );

        it('should set set success to OK upon successful activation',
            inject([Verify],
                fakeAsync((service: Verify) => {
                    spyOn(service, 'get').and.returnValue(Observable.of({}));

                    comp.ngOnInit();
                    tick();

                    expect(comp.error).toBe(null);
                    expect(comp.success).toEqual('OK');
                })
            )
        );

        it('should set set error to ERROR upon activation failure',
            inject([Verify],
                fakeAsync((service: Verify) => {
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
