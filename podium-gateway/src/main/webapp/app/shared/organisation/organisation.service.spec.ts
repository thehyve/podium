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
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { OrganisationService } from './organisation.service';
import { Organisation } from './organisation.model';


describe('OrganisationService (Mocked)', () => {
    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                OrganisationService,
            ]
        });
    });

    it('should construct', waitForAsync(
        inject([OrganisationService],
            (service) => {
                expect(service).toBeDefined();
            })
    ));

    describe('jsonArrayToOrganisations', () => {

        const _organisations = [new Organisation({
            id: 1,
            uuid: "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
            name: "International variable name bank",
            shortName: "VarnameBank",
            activated: true,
            organisationUuid: "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a"
        })];

        it('should convert array of json object to organisations', waitForAsync(
            inject([OrganisationService], (service) => {
                let _jsonArray = [{
                    "id": 1,
                    "uuid": "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
                    "name": "International variable name bank",
                    "shortName": "VarnameBank",
                    "activated": true,
                    "organisationUuid": "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a"
                }];
                let res = service.jsonArrayToOrganisations(_jsonArray);

                expect(res).toEqual(_organisations);
            })));

    });
});
