/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { TestBed, waitForAsync, tick, fakeAsync, inject } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MockActivatedRoute, MockRouter } from '../../../../../test/javascript/spec/helpers/mock-route.service';
import { Verify } from './verify.service';
import { VerifyComponent } from './verify.component';
import { PodiumTestModule } from '../../../../../test/javascript/spec/test.module';


describe('Component Tests', () => {

    describe('VerifyComponent', () => {

        let comp: VerifyComponent;

        beforeEach(waitForAsync(() => {
            TestBed.configureTestingModule({
                imports: [PodiumTestModule],
                declarations: [VerifyComponent],
                providers: [
                    Verify,
                    {
                        provide: ActivatedRoute,
                        useValue: new MockActivatedRoute({'key': 'ABC123'})
                    },
                    {
                        provide: Router,
                        useValue: new MockRouter()
                    }
                ]
            }).overrideTemplate(VerifyComponent, '')
                .compileComponents();
        }));

        beforeEach(() => {
            let fixture = TestBed.createComponent(VerifyComponent);
            comp = fixture.componentInstance;
        });

        it('calls activate.get with the key from params',
            inject([Verify],
                fakeAsync((service: Verify) => {
                    spyOn(service, 'get').and.returnValue(of());

                    comp.ngOnInit();
                    tick();

                    expect(service.get).toHaveBeenCalledWith('ABC123');
                })
            )
        );

        it('should set set success to OK upon successful activation',
            inject([Verify],
                fakeAsync((service: Verify) => {
                    spyOn(service, 'get').and.returnValue(of({}));

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
                    spyOn(service, 'get').and.returnValue(throwError('ERROR'));

                    comp.ngOnInit();
                    tick();

                    expect(comp.error).toBe('ERROR');
                    expect(comp.success).toEqual(null);
                })
            )
        );
    });
});
