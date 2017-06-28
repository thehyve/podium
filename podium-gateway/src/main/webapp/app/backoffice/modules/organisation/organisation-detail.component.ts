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
import { OrganisationService } from '../../../shared/organisation/organisation.service';
import { Organisation } from '../../../shared/organisation/organisation.model';

@Component({
    selector: 'pdm-organisation-detail',
    templateUrl: './organisation-detail.component.html',
    providers: [OrganisationService]
})
export class OrganisationDetailComponent implements OnInit, OnDestroy {

    organisation: Organisation;
    private subscription: any;

    constructor(
        private organisationService: OrganisationService,
        private route: ActivatedRoute
    ) {

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
