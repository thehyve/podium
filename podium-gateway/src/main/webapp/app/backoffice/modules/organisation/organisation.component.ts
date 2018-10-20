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
import { Response, Http } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Rx';
import { JhiEventManager, JhiParseLinks } from 'ng-jhipster';
import { Principal } from '../../../shared';
import { Overview } from '../../../shared/overview/overview';
import { OverviewService } from '../../../shared/overview/overview.service';
import { OverviewServiceConfig } from '../../../shared/overview/overview.service.config';
import { Organisation } from '../../../shared/organisation/organisation.model';
import { OrganisationService } from '../../../shared/organisation/organisation.service';

let overviewConfig: OverviewServiceConfig = {
    resourceUrl: 'podiumuaa/api/organisations',
    resourceSearchUrl: 'podiumuaa/api/_search/organisations'
};

@Component({
    selector: 'pdm-organisation',
    templateUrl: './organisation.component.html',
    providers: [
        {
            provide: OverviewService,
            useFactory: (http: Http) => {
                return new OverviewService(http, overviewConfig);
            },
            deps: [
                Http
            ]
        }
    ]
})
export class OrganisationComponent extends Overview implements OnInit, OnDestroy {

    currentAccount: any;
    organisations: Organisation[];
    error: any;
    success: any;
    eventSubscriber: Subscription;
    overviewSubscription: Subscription;

    constructor(
        private organisationService: OrganisationService,
        private overviewService: OverviewService,
        private parseLinks:  JhiParseLinks,
        private principal: Principal,
        protected activatedRoute: ActivatedRoute,
        protected router: Router,
        private eventManager:  JhiEventManager
    ) {
        super(router, activatedRoute);

        this.overviewSubscription = this.overviewService.onOverviewUpdate.subscribe(
            (res: Response) => this.processAvailableOrganisations(res.json(), res.headers)
        );
    }

    ngOnInit() {
        this.fetchOrganisations();
        this.principal.identity().then((account) => {
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
            .subscribe((res: Response) => this.overviewService.overviewUpdateEvent(res));
    }

    transitionOrganisations() {
        this.transition();
        this.fetchOrganisations();
    }

    toggleActivated (organisation) {
        organisation.activated = !organisation.activated;
        this.organisationService.activate(organisation.uuid, organisation.activated).subscribe(
            (res: Response) => {
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

    private processAvailableOrganisations(organisations, headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('x-total-count');
        this.queryCount = this.totalItems;
        this.organisations = organisations;
    }

}
