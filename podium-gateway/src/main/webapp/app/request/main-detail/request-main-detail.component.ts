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
import { AlertService } from 'ng-jhipster';
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
     * Setup component as ViewChild to access methods inside child.
     * Used for review and method accessors in sibling components
     */
    @ViewChild(RequestDetailComponent)
    private requestDetail: RequestDetailComponent;

    public request: RequestBase;
    public error: any;
    public success: any;

    constructor(
        private route: ActivatedRoute,
        private requestService: RequestService,
        private alertService: AlertService
    ) {

        this.requestService.onRequestUpdate.subscribe((request: RequestBase) => {
            this.request = request;
        });
    }

    ngOnInit() {
        this.route.data
            .subscribe((data: { request: RequestBase }) => {
                this.request = data.request;
                this.onSuccess(data.request);
            }, err => this.onError(err));
    }

    private onSuccess(request: RequestBase) {
        this.request = request;
        this.requestDetail.setRequest(request);
    }

    private onError(error) {
        this.alertService.error(error.error, error.message, null);
        this.success = null;
    }
}
