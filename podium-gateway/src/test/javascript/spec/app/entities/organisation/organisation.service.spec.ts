/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { async, inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { OrganisationService } from '../../../../../../main/webapp/app/backoffice/modules/organisation/organisation.service';

describe('OrganisationService (Mocked)', () => {
    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                OrganisationService,

                MockBackend,
                BaseRequestOptions,
                {
                    provide: Http,
                    useFactory: (backend, options) => new Http(backend, options),
                    deps: [MockBackend, BaseRequestOptions]
                }
            ],
            imports: [
                HttpModule
            ]
        });
    });

    it('should construct', async(
        inject([OrganisationService, MockBackend],
            (service, mockBackend) => {
                expect(service).toBeDefined();
            })
    ));

    describe('findAll', () => {
        const mockResponse = [ {
            "id" : 1,
            "uuid" : "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
            "name" : "International variable name bank",
            "shortName" : "VarnameBank",
            "activated" : true,
            "organisationUuid" : "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a"
        }, {
            "id" : 1000,
            "uuid" : "549d67f8-7720-423a-ada9-bea83760e06a",
            "name" : "International VarnameBank2",
            "shortName" : "VarnameBank2",
            "activated" : false,
            "organisationUuid" : "549d67f8-7720-423a-ada9-bea83760e06a"
        }];

        it('should parse response', async(inject(
            [OrganisationService, MockBackend], (service, mockBackend) => {

                mockBackend.connections.subscribe(conn => {
                    conn.mockRespond(new Response(new ResponseOptions({body: JSON.stringify(mockResponse)})));
                });

                const result = service.findAll();

                result.subscribe(res => {
                    expect(res).toEqual([ {
                        "id" : 1,
                        "uuid" : "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
                        "name" : "International variable name bank",
                        "shortName" : "VarnameBank",
                        "activated" : true,
                        "organisationUuid" : "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a"
                    }, {
                        "id" : 1000,
                        "uuid" : "549d67f8-7720-423a-ada9-bea83760e06a",
                        "name" : "International VarnameBank2",
                        "shortName" : "VarnameBank2",
                        "activated" : false,
                        "organisationUuid" : "549d67f8-7720-423a-ada9-bea83760e06a"
                    }]);
                });
            })));
    });
});