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
import { JhiLanguageService } from 'ng-jhipster';
import { Response } from '@angular/http';
import { RequestType } from '../request/request-type';
import { Observable } from 'rxjs';
import { Organisation } from '../../backoffice/modules/organisation/organisation.model';
import { OrganisationService } from '../../backoffice/modules/organisation/organisation.service';

@Component({
    selector: 'pdm-organisation-selector',
    templateUrl: './organisation-selector.component.html',
    styleUrls: ['./organisation-selector.scss']
})

export class OrganisationSelectorComponent implements OnInit {

    organisationOptions: Organisation[];
    selectedOrganisations: Organisation[];

    @Input() organisations: Organisation[];
    @Input() requestType: RequestType[];

    @Output() organisationChange = new EventEmitter<Organisation[]>();

    private static onError (error) {
        return Observable.throw(new Error(error.status));
    }

    constructor(private jhiLanguageService: JhiLanguageService,
                private organisationService: OrganisationService
    ) {}

    onChange() {
        this.organisations = this.selectedOrganisations;
        this.organisationChange.emit(this.organisations);
    }

    ngOnInit() {
        this.selectedOrganisations = this.organisations;
        this.loadOrganisations();
    }

    private loadOrganisations() {
        this.organisationService.findAll()
            .subscribe(
                (data: Response) => {
                    if (Array.isArray(data)) {
                        this.organisationOptions = data;
                    }
                },
                (res: Response) => {
                    return OrganisationSelectorComponent.onError(res.json());
                }
            );
    }

}
