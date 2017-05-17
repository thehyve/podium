/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Response } from '@angular/http';
import { RequestType } from '../request/request-type';
import { Observable } from 'rxjs';
import { OrganisationService } from '../../backoffice/modules/organisation/organisation.service';
import { organisationRoute } from '../../backoffice/modules/organisation/organisation.route';

@Component({
    selector: 'pdm-organisation-selector',
    templateUrl: './organisation-selector.component.html'
})

export class OrganisationSelectorComponent implements OnInit {

    allOrganisations: any[];
    selectedOrganisations: any[];
    organisationOptions: any;
    selectedOrganisationUuids: any[];

    @Output() organisationChange = new EventEmitter();

    @Input() requestTypes: RequestType[];
    @Input()
    get organisations() {
        return this.selectedOrganisations;
    }
    set organisations(val) {
        this.selectedOrganisations = val;
        this.organisationChange.emit(this.selectedOrganisations);
    }

    private static onError (error) {
        return Observable.throw(new Error(error.status));
    }

    constructor(
        private organisationService: OrganisationService
    ) { }

    onChange() {
        // get organisation instance of selected uuid
        this.organisations = this.selectedOrganisationUuids.map(
            (selected) => {
                return this.organisationOptions.find( (option) => {
                    return option.uuid === selected;
                });
            }
        );
        this.organisationChange.emit(this.organisations);
    }

    filterOptionsByRequestType() {
        // Reset values
        this.organisations = [];
        this.selectedOrganisationUuids = [];
        // Show only organisations which are associated with selected request type
        this.loadOrganisationsByRequestTypes();
    }

    loadOrganisationsByRequestTypes() {
        this.organisationOptions = [];
        for (let organisation of this.allOrganisations) {
            if (organisation.hasRequestTypes(this.requestTypes)) {
                this.organisationOptions.push(organisation);
            }
        }
    }

    ngOnInit() {
        // set selected organisations
        this.selectedOrganisationUuids = this.organisations.map( organisation => {
            return organisation.uuid;
        });
        // load organisation options
        this.organisationService.findAllAvailable()
            .subscribe(
                (data) => {
                    this.allOrganisations = this.organisationService.jsonArrayToOrganisations(data);
                    this.selectedOrganisations =
                        this.organisationService.convertUuidsToOrganisations(this.organisations, this.allOrganisations);
                    this.loadOrganisationsByRequestTypes();
                },
                (res: Response) => {
                    return OrganisationSelectorComponent.onError(res.json());
                }
            );
    }
}
