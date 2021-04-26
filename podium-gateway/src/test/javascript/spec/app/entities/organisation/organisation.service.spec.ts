/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { waitForAsync, inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Response, ResponseOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { PodiumTestModule } from '../../../test.module';
import { OrganisationService } from '../../../../../../main/webapp/app/shared/organisation/organisation.service';
import { Organisation } from '../../../../../../main/webapp/app/shared/organisation/organisation.model';


describe('OrganisationService (Mocked)', () => {
    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [PodiumTestModule],
            providers: [
                OrganisationService,
                MockBackend,
                BaseRequestOptions
            ]
        });
    });

    it('should construct', waitForAsync(
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

        it('should parse response', waitForAsync(inject(
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

    describe('jsonArrayToOrganisations', () => {

        const _organisations = [new Organisation({
            id: 1,
            uuid: "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
            name: "International variable name bank",
            shortName: "VarnameBank",
            activated : true,
            organisationUuid: "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a"
        })];

        it('should convert array of json object to organisations', waitForAsync(inject([OrganisationService], (service) => {
            let _jsonArray = [{
                "id" : 1,
                "uuid" : "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
                "name" :  "International variable name bank",
                "shortName" : "VarnameBank",
                "activated" : true,
                "organisationUuid" : "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a"
            }];
            let res = service.jsonArrayToOrganisations(_jsonArray);

            expect(res).toEqual(_organisations);
        })));

    });
});
