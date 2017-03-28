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
import { Organisation } from '../../entities/organisation/organisation.model';

@Component({
    selector: 'pdm-organisation-selector',
    templateUrl: './organisation-selector.component.html',
    styleUrls: ['./organisation-selector.scss']
})

export class OrganisationSelectorComponent implements OnInit {

    organisationValues: Organisation[];
    organisationOptions: any;
    selectedOrganisations: Organisation[];

    @Output() organisationChange = new EventEmitter();

    @Input()
    get organisations() {
        return this.organisationValues;
    }

    set organisations(val) {
        this.organisationValues = val;
        this.organisationChange.emit(this.organisationValues);
    }

    constructor(private jhiLanguageService: JhiLanguageService,
                private organisationService: OrganisationService
    ){}

    onChange() {
        console.log(this.selectedOrganisations);
        this.organisations = this.selectedOrganisations;
        this.organisationChange.emit(this.organisations);
    }

    ngAfterContentInit() {

    }

    ngOnInit() {
        this.selectedOrganisations = this.organisations;
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

    }

}
