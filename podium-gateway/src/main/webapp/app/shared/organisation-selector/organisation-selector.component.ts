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

@Component({
    selector: 'pdm-organisation-selector',
    templateUrl: './organisation-selector.component.html',
    styleUrls: ['./organisation-selector.scss']
})

export class OrganisationSelectorComponent implements OnInit {

    selectedOrganisationValues: any[];
    organisationOptions: any;
    selectedOrganisations: any[];

    @Output() organisationChange = new EventEmitter();

    @Input() requestType: RequestType[];
    @Input()
    get organisations() {
        return this.selectedOrganisationValues;
    }
    set organisations(val) {
        this.selectedOrganisationValues = val;
        this.organisationChange.emit(this.selectedOrganisationValues);
    }


    private static onError (error) {
        return Observable.throw(new Error(error.status));
    }

    constructor(private organisationService: OrganisationService){}

    onChange() {
        // get organisation instance of selected uuid
        this.organisations = this.selectedOrganisations.map(
            (selected) => {
                return this.organisationOptions.find( (option) => {
                    return option.uuid == selected;
                })
            }
        );
        this.organisationChange.emit(this.organisations);
    }

    ngOnInit() {
        // set selected organisations
        this.selectedOrganisations = this.organisations;
        this.selectedOrganisations = this.selectedOrganisations.map( organisation => {
            return organisation.uuid;
        });
        // load organisation options
        this.organisationService.findAllAvailable()
            .subscribe(
                (data: Response) => {
                    this.organisationOptions = this.organisationService.jsonArrayToOrganisations(data);
                },
                (res: Response) => {
                    return OrganisationSelectorComponent.onError(res.json());
                }
            );
    }
}
