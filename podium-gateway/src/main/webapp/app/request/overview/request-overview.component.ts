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
import { JhiLanguageService } from 'ng-jhipster';
import { RequestBase } from '../../shared/request/request-base';
import { RequestService } from '../../shared/request/request.service';
import { User } from '../../shared/user/user.model';
import { Router, ActivatedRoute } from '@angular/router';
import { Principal } from '../../shared';
import { RequestFormService } from '../form/request-form.service';

@Component({
    selector: 'pdm-request-overview',
    templateUrl: './request-overview.component.html',
    styleUrls: ['request-overview.scss']
})

export class RequestOverviewComponent implements OnInit {

    private currentUser: User;

    availableRequests: RequestBase[];
    error: string;
    success: string;

    constructor(private jhiLanguageService: JhiLanguageService,
                private requestService: RequestService,
                private router: Router,
                private requestFormService: RequestFormService,
                private activatedRoute: ActivatedRoute,
                private principal: Principal) {
        this.jhiLanguageService.setLocations(['request']);
    }

    ngOnInit(): void {
        this.principal.identity().then((account) => {
            this.currentUser = account;
            this.displaySubmittedRequests();
        });
    }

    createNewRequest() {
        this.requestFormService.request = null;
        this.router.navigate(['./requests/new']);
    }

    displayDrafts() {
        this.requestService.findDrafts()
            .subscribe(
                (requestDrafts) => this.processAvailableRequests(requestDrafts),
                (error) => this.onError('Error loading available request drafts.')
            );
    }

    displaySubmittedRequests() {
        this.requestService.findSubmittedRequests()
            .subscribe(
                (requests) => this.processAvailableRequests(requests),
                (error) => this.onError('Error loading available request drafts.')
            );
    }

    editRequest(request) {
        this.requestFormService.request = request;
        this.router.navigate(['./requests/edit']);
    }

    navigateToRequestDetail(request) {
        this.router.navigate(['./requests/detail', request.uuid]);
    }

    processAvailableRequests(requests) {
        this.availableRequests = requests;
    }

    transition() {
        this.displaySubmittedRequests();
    }

    private onSuccess(result) {
        this.error =  null;
        this.success = 'SUCCESS';
        window.scrollTo(0, 0);
    }

    private onError(error) {
        this.error =  'ERROR';
        this.success = null;
        window.scrollTo(0, 0);
    }

}
