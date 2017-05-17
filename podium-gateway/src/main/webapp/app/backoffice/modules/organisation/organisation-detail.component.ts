/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { Organisation } from './organisation.model';
import { OrganisationService } from './organisation.service';

@Component({
    selector: 'jhi-organisation-detail',
    templateUrl: './organisation-detail.component.html',
    providers: [OrganisationService]
})
export class OrganisationDetailComponent implements OnInit, OnDestroy {

    organisation: Organisation;
    private subscription: any;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private organisationService: OrganisationService,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['organisation']);
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe(params => {
            this.load(params['uuid']);
        });
    }

    load (uuid) {
        this.organisationService.findByUuid(uuid).subscribe(organisation => {
            this.organisation = organisation;
        });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

}
