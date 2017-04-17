/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit, ViewChild } from '@angular/core';
import { JhiLanguageService } from 'ng-jhipster';
import { RequestBase } from '../../shared/request/request-base';
import { ActivatedRoute } from '@angular/router';
import { RequestService } from '../../shared/request/request.service';
import { RequestDetailComponent } from './detail/request-detail.component';

@Component({
    selector: 'pdm-request-main-detail',
    templateUrl: './request-main-detail.component.html',
    styleUrls: ['request-main-detail.scss']
})

export class RequestMainDetailComponent implements OnInit {

    /**
     * Setup component as viewchild to access methods inside child.
     * Used for review and method accessors in sibling components
     */
    @ViewChild(RequestDetailComponent)
    private requestDetail: RequestDetailComponent;

    public request: RequestBase;
    public error: any;
    public success: any;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private route: ActivatedRoute,
        private requestService: RequestService
    ) {
        this.jhiLanguageService.setLocations(['request']);
    }

    ngOnInit() {

        /**
         * Resolve request
         */
        this.route.params.subscribe(params => {
            let uuid = params['uuid'];
            if (uuid) {
                this.requestService.findByUuid(uuid).subscribe(
                    (request) => this.onSuccess(request),
                    (res) => this.onError(res)
                );
            }
        });
    }

    private onSuccess(request: RequestBase) {
        this.request = request;
        this.requestDetail.setRequest(request);
    }

    private onError(error) {
        this.error =  'ERROR';
        this.success = null;
    }}
