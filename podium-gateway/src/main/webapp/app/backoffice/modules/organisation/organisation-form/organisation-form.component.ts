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
import { HttpErrorResponse } from '@angular/common/http';
import { AlertService } from '../../../../core/util/alert.service';
import { AccountService } from '../../../../core/auth/account.service';
import { User } from '../../../../shared/user/user.model';
import { ActivatedRoute, Router } from '@angular/router';
import { RequestType } from '../../../../shared/request/request-type';
import { Organisation } from '../../../../shared/organisation/organisation.model';
import { OrganisationService } from '../../../../shared/organisation/organisation.service';
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'pdm-organisation-form',
    templateUrl: './organisation-form.component.html',
    styleUrls: ['organisation-form.scss']
})
export class OrganisationFormComponent implements OnInit {

    currentAccount: User;
    organisation: Organisation;
    error: any;
    success: any;
    isSaving: boolean;
    requestTypes: any = RequestType;

    constructor(
        private organisationService: OrganisationService,
        private alertService: AlertService,
        private translateService: TranslateService,
        private accountService: AccountService,
        private route: ActivatedRoute,
        private router: Router
    ) {

    }

    ngOnInit() {
        this.accountService.identity().subscribe((account) => {
            this.currentAccount = account;
        });

        this.route.params.subscribe(params => {
            let uuid = params['uuid'];
            if (uuid) {
                this.organisationService.findByUuid(uuid).subscribe(
                    (organisation) => { this.organisation = organisation; },
                    (res) => this.onError(res)
                );
            } else {
                this.organisation = new Organisation();
                this.organisation.requestTypes = [];
                this.organisation.roles = [];
            }
        });
    }

    onSaveSuccess (result, isCreate: boolean) {
        if (isCreate) {
            this.organisation = result;
        }

        let notification = isCreate ? 'organisation.saved' : 'organisation.updated';

        this.alertService.success(this.translateService.instant(notification));
        this.isSaving = false;
    }

    onSaveError (error: HttpErrorResponse) {
        this.isSaving = false;
        this.onError(error);
    }

    onError (error: HttpErrorResponse) {
        this.alertService.error(error.message, null, null);
    }

    save () {
        this.isSaving = true;
        if (this.organisation.uuid) {
            this.organisationService.update(this.organisation)
                .subscribe(
                    (res) => this.onSaveSuccess(res, false),
                    (res: HttpErrorResponse) => this.onSaveError(res)
                );
        } else {
            this.organisationService.create(this.organisation)
                .subscribe(
                    (res) => this.onSaveSuccess(res, true),
                    (res: HttpErrorResponse) => this.onSaveError(res)
                );
        }
    }

    cancel() {
        return this.router.navigate(['/bbmri/organisation']);
    }

    updateRequestType(selectedRequestType) {
        let _idx = this.organisation.requestTypes.indexOf(selectedRequestType.value);
        if ( _idx < 0) {
            this.organisation.requestTypes.push(selectedRequestType.value);
        } else {
            this.organisation.requestTypes.splice(_idx, 1);
        }
    }

    get canActivateOrganisation() {
        let authorities = this.currentAccount?.authorities || [];
        return authorities.includes('ROLE_BBMRI_ADMIN');
    }
}
