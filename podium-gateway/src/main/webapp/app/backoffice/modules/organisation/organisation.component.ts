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
import { HttpClient, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { parseLinks } from '../../../shared/util/parse-links-util';
import { EventManager } from '../../../core/util/event-manager.service';
import { AccountService } from '../../../core/auth/account.service';
import { Account } from '../../../core/auth/account.model';
import { Overview } from '../../../shared/overview/overview';
import { OverviewService } from '../../../shared/overview/overview.service';
import { OverviewServiceConfig } from '../../../shared/overview/overview.service.config';
import { Organisation } from '../../../shared/organisation/organisation.model';
import { OrganisationService } from '../../../shared/organisation/organisation.service';

let overviewConfig: OverviewServiceConfig = {
    resourceUrl: 'podiumuaa/api/organisations',
};

@Component({
    selector: 'pdm-organisation',
    templateUrl: './organisation.component.html',
    providers: [
        {
            provide: OverviewService,
            useFactory: (http: HttpClient) => {
                return new OverviewService(http, overviewConfig);
            },
            deps: [
                HttpClient
            ]
        }
    ]
})
export class OrganisationComponent extends Overview implements OnInit, OnDestroy {

    currentAccount: Account;
    organisations: Organisation[];
    error: any;
    success: any;
    eventSubscriber: Subscription;
    overviewSubscription: Subscription;

    constructor(
        private organisationService: OrganisationService,
        private overviewService: OverviewService,
        private accountService: AccountService,
        protected activatedRoute: ActivatedRoute,
        protected router: Router,
        private eventManager: EventManager
    ) {
        super(router, activatedRoute);

        this.overviewSubscription = this.overviewService.onOverviewUpdate.subscribe(
            (res: HttpResponse<Organisation[]>) =>
                this.processAvailableOrganisations(res.body, res.headers)
        );
    }

    ngOnInit() {
        this.fetchOrganisations();
        this.accountService.identity().then((account) => {
            this.currentAccount = account;
        });

        this.registerChangeInOrganisations();
    }

    ngOnDestroy() {
        if (this.eventSubscriber) {
            this.eventManager.destroy(this.eventSubscriber);
        }

        if (this.overviewSubscription) {
            this.overviewSubscription.unsubscribe();
        }
    }

    trackUuid (index: number, item: Organisation) {
        return item.uuid;
    }

    registerChangeInOrganisations() {
        this.eventSubscriber = this.eventManager.subscribe('organisationListModification', () => {
            this.fetchOrganisations();
        });
    }

    fetchOrganisations() {
        this.overviewService
            .findOrganisationsForOverview(this.getPageParams())
            .subscribe((res) => this.overviewService.overviewUpdateEvent(res));
    }

    transitionOrganisations() {
        this.transition();
        this.fetchOrganisations();
    }

    toggleActivated (organisation) {
        organisation.activated = !organisation.activated;
        this.organisationService.activate(organisation.uuid, organisation.activated).subscribe(
            (res) => {
                if (res.status === 200) {
                    this.error = null;
                    this.success = 'OK';
                    this.fetchOrganisations();
                } else {
                    this.success = null;
                    this.error = 'ERROR';
                }
            }
        );
    }

    private processAvailableOrganisations(organisations: Organisation[], headers) {
        this.links = parseLinks(headers.get('link'));
        this.totalItems = headers.get('x-total-count');
        this.queryCount = this.totalItems;
        this.organisations = organisations;
    }

}
