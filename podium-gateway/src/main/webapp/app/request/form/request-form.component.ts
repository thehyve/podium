/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { RequestDetail } from '../../shared/request/request-detail';
import { RequestFormService } from './request-form.service';
import { RequestType } from '../../shared/request/request-type';
import { FileSelectDirective, FileDropDirective, FileUploader } from 'ng2-file-upload/ng2-file-upload';
import { OrganisationService } from '../../entities/organisation/organisation.service';
import { Organisation } from '../../entities/organisation/organisation.model';



@Component({
    selector: 'pdm-request-form',
    templateUrl: './request-form.component.html',
    styleUrls: ['request-form.scss']
})
export class RequestFormComponent implements OnInit {

    error: string;
    success: string;
    request: RequestDetail = new RequestDetail();
    requestTypes = RequestType;
    availableOrganisations: Organisation[];



    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestFormService: RequestFormService,
        private route: ActivatedRoute,
        private router: Router,
        private organisationService: OrganisationService
    ) {
        this.jhiLanguageService.setLocations(['request']);
    }

    ngOnInit () {
        /**
         * Organisation resolve
         */
        this.organisationService.findAvailable().map((availableOrganisations) => {
            // this.availableOrganisations = availableOrganisations;
        });

        /**
         * Resolve Tags
         */

        /**
         * Resolve Drafts
         */


    }



}
