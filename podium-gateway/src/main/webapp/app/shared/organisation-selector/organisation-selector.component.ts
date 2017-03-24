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
import { OrganisationService } from '../../entities/organisation/organisation.service';
import { Response } from '@angular/http';

@Component({
    selector: 'pdm-organisation-selector',
    templateUrl: './organisation-selector.component.html',
    styleUrls: ['./organisation-selector.scss']
})

export class OrganisationSelectorComponent implements OnInit {

    organisationValue: string;
    organisationOptions: any;
    selectedOrganisation: string;


    constructor(private jhiLanguageService: JhiLanguageService,
                private organisationService: OrganisationService
    ) {}

    onChange() {
    }

    ngAfterContentInit() {
    }

    ngOnInit() {
        this.loadOrganisations();
    }

    private loadOrganisations() {
        this.organisationService.findAll().subscribe(
            (res) => {
                this.organisationOptions = res;
            },
            (res: Response) => this.onError(res.json())
        );
    }

    private onError (error) {
        console.error(error);
    }

}
