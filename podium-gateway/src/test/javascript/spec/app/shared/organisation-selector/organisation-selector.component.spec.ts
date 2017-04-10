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
import { TranslateService, TranslateLoader, TranslateParser } from 'ng2-translate';
import { MockLanguageService } from '../../../helpers/mock-language.service';
import { BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { OrganisationService } from '../../../../../../main/webapp/app/backoffice/modules/organisation/organisation.service';
import { Organisation } from '../../../../../../main/webapp/app/backoffice/modules/organisation/organisation.model';

describe('OrganisationSelectorComponent (templateUrl)', () => {

    let comp: OrganisationSelectorComponent;
    let fixture: ComponentFixture<OrganisationSelectorComponent>;

    // async beforeEach, since we use external templates & styles
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                BaseRequestOptions,
                MockBackend,
                JhiLanguageService,
                TranslateService,
                TranslateLoader,
                TranslateParser,
                OrganisationService,
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
            ],
            imports: [FormsModule],
            declarations: [OrganisationSelectorComponent], // declare the test component
        }).overrideComponent(OrganisationSelectorComponent, {
            set: {
                template: ''
            }
        }).compileComponents();
    }));

    // synchronous beforeEach
    beforeEach(() => {
        fixture = TestBed.createComponent(OrganisationSelectorComponent);
        comp = fixture.componentInstance; // OrganisationSelectorComponent test instance
    });

    it('should not have organisation options and selected organisations', () => {
        fixture.detectChanges();
        expect(comp.selectedOrganisations).toBe(undefined);
        expect(comp.organisationOptions).toBe(undefined);
    });


    it('should select organisation(s) based on input value on initialisation', () => {
        fixture.detectChanges();
        let dummyOrganisation = new Organisation();
        dummyOrganisation.id = 1000;
        dummyOrganisation.name = 'dummy';
        comp.organisations = [dummyOrganisation];
        comp.ngOnInit();
        expect(comp.selectedOrganisations).toEqual(comp.organisations);
    });

    it('should update input value when selected organisations changed', () => {
        fixture.detectChanges();
        let dummyOrganisation = new Organisation();
        dummyOrganisation.id = 1000;
        dummyOrganisation.name = 'dummy';
        comp.selectedOrganisations = [dummyOrganisation];
        comp.onChange();
        expect(comp.organisations).toEqual(comp.selectedOrganisations);
    });

});
