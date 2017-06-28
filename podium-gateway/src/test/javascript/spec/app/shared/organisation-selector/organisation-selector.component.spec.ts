/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { OrganisationSelectorComponent }
    from '../../../../../../main/webapp/app/shared/organisation-selector/organisation-selector.component';
import { JhiLanguageService } from 'ng-jhipster';
import { TranslateService, TranslateLoader, TranslateParser } from '@ngx-translate/core';
import { BaseRequestOptions } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { EventEmitter } from '@angular/core';
import { RequestType } from '../../../../../../main/webapp/app/shared/request/request-type';
import { PodiumTestModule } from '../../../test.module';
import { OrganisationService } from '../../../../../../main/webapp/app/shared/organisation/organisation.service';
import { Organisation } from '../../../../../../main/webapp/app/shared/organisation/organisation.model';

describe('OrganisationSelectorComponent (templateUrl)', () => {

    let comp: OrganisationSelectorComponent;
    let fixture: ComponentFixture<OrganisationSelectorComponent>;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                FormsModule,
                PodiumTestModule
            ],
            providers: [
                BaseRequestOptions,
                MockBackend,
                JhiLanguageService,
                TranslateService,
                TranslateLoader,
                TranslateParser,
                OrganisationService,
            ],
            declarations: [OrganisationSelectorComponent], // declare the test component
        }).overrideTemplate(OrganisationSelectorComponent, '')
            .compileComponents();
    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(OrganisationSelectorComponent);
        comp = fixture.componentInstance; // OrganisationSelectorComponent test instance
    });

    it('should not have organisation options and selected organisations', () => {
        expect(comp.selectedOrganisations).toBe(undefined);
        expect(comp.selectedOrganisationUuids).toBe(undefined);
        expect(comp.organisationOptions).toBe(undefined);
    });

    describe('ngOnInit', () => {
        it('should select organisation(s) based on input value on initialisation', () => {
            comp.organisations = [new Organisation({id: 1000, uuid: '123', name: 'dummy'})];
            comp.ngOnInit();
            expect(comp.selectedOrganisationUuids).toEqual(['123']);
        });
    });

    describe('onChange', () => {

        beforeEach(() => {
            comp.organisationOptions = [
                new Organisation({id: 1000, uuid: '123', name: 'dummy'}),
                new Organisation({id: 1001, uuid: '456', name: 'dummy'})
            ];
            comp.selectedOrganisationUuids = ['456'];
            comp.organisationChange = new EventEmitter();

            spyOn(comp.organisationChange, 'emit');
        });

        it('should update input value when selected organisations changed', () => {
            comp.onChange();
            expect(comp.organisations).toEqual([new Organisation({id: 1001, uuid: '456', name: 'dummy'})]);
        });

        it('should emit value when selection changed', () => {
            comp.onChange();
            expect(comp.organisationChange.emit).toHaveBeenCalled();
        });

    });

    describe('filterOptionsByRequestType', () => {
        beforeEach(() => {
            comp.requestTypes = [
                RequestType.Data,
                RequestType.Material,
                RequestType.Images
            ];
            comp.allOrganisations = [
                new Organisation({id: 1000, uuid: '123', name: 'dummy', requestTypes: [RequestType.Data]}),
                new Organisation({
                    id: 1001,
                    uuid: '456',
                    name: 'dummy',
                    requestTypes: [RequestType.Data, RequestType.Images]
                })
            ];
            comp.organisations = [new Organisation({id: 1001, uuid: '456', name: 'dummy'})];
            comp.selectedOrganisationUuids = ['456'];

            spyOn(comp, 'loadOrganisationsByRequestTypes');
        });

        it('should empty selected values and load organisations by request type', () => {
            comp.filterOptionsByRequestType();
            expect(comp.organisations.length).toBe(0);
            expect(comp.selectedOrganisations.length).toBe(0);
            expect(comp.loadOrganisationsByRequestTypes).toHaveBeenCalled();
        });
    });

    describe('loadOrganisationsByRequestTypes', () => {

        beforeEach(() => {
            comp.requestTypes = [RequestType.Images];
            comp.allOrganisations = [
                new Organisation({id: 1000, uuid: '123', name: 'dummy', requestTypes: [RequestType.Data]}),
                new Organisation({
                    id: 1001,
                    uuid: '456',
                    name: 'dummy',
                    requestTypes: [RequestType.Data, RequestType.Images]
                })
            ];
            comp.organisations = [new Organisation({id: 1001, uuid: '456', name: 'dummy'})];
            comp.selectedOrganisationUuids = ['456'];

        });

        it('should load organisation options by selected request types', () => {
            comp.loadOrganisationsByRequestTypes();
            expect(comp.organisationOptions.length).toBe(1);
        });
    });
});
