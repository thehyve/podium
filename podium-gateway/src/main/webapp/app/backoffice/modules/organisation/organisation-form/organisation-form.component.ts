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
import { Subscription } from 'rxjs/Rx';
import { EventManager, JhiLanguageService, AlertService } from 'ng-jhipster';

import { Organisation } from '../organisation.model';
import { OrganisationService } from '../organisation.service';
import { Principal } from '../../../../shared';
import { User } from '../../../../shared/user/user.model';
import { Response } from '@angular/http';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'pdm-organisation-form',
    templateUrl: './organisation-form.component.html'
})
export class OrganisationFormComponent implements OnInit, OnDestroy {

    currentAccount: User;
    organisation: Organisation;
    error: any;
    success: any;
    eventSubscriber: Subscription;
    isSaving: boolean;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private organisationService: OrganisationService,
        private alertService: AlertService,
        private principal: Principal,
        private eventManager: EventManager,
        private route: ActivatedRoute
    ) {
        this.jhiLanguageService.setLocations(['organisation']);
    }

    ngOnInit() {
        this.principal.identity().then((account) => {
            this.currentAccount = account;
        });

        this.route.params.subscribe(params => {
            let uuid = params['uuid'];
            if (uuid) {
                this.organisationService.findByUuid(uuid).subscribe(organisation => {
                    this.organisation = organisation;
                });
            } else {
                this.organisation = new Organisation();
            }
        });

    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    private onSaveSuccess (result) {
        // this.eventManager.broadcast({ name: 'organisationListModification', content: 'OK'});
        this.isSaving = false;
    }

    private onSaveError (error) {
        this.isSaving = false;
        this.onError(error);
    }

    private onError (error) {
        this.alertService.error(error.message, null, null);
    }

    save () {
        this.isSaving = true;
        if (this.organisation.uuid !== undefined) {
            this.organisationService.update(this.organisation)
                .subscribe((res: Response) => this.onSaveSuccess(res), (res: Response) => this.onSaveError(res.json()));
        } else {
            this.organisationService.create(this.organisation)
                .subscribe((res: Response) => this.onSaveSuccess(res), (res: Response) => this.onSaveError(res.json()));
        }
    }
}
