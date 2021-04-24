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
import { ActivatedRoute, Router } from '@angular/router';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { EventManager } from '../../../core/util/event-manager.service';
import { UserModalService } from './user-modal.service';
import { User } from '../../../shared/user/user.model';
import { UserService } from '../../../shared/user/user.service';
import { AUTHORITIES_MAP } from '../../../shared/authority/authority.constants';
import { RouterHelper } from '../../../shared/util/router-helper';

@Component({
    selector: 'pdm-user-mgmt-dialog',
    templateUrl: './user-management-dialog.component.html'
})
export class UserMgmtDialogComponent implements OnInit {

    user: User;
    languages: any[];
    authorities: any[];
    isSaving: Boolean;
    userAuthorityMap;

    constructor (
        public activeModal: NgbActiveModal,
        private translateService: TranslateService,
        private userService: UserService,
        private eventManager: EventManager,
        private router: Router
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_RESEARCHER'];
        this.languages = this.translateService.getLangs();

        this.userAuthorityMap = AUTHORITIES_MAP;
    }

    clear() {
        this.activeModal.dismiss('cancel');
        this.router.navigate([RouterHelper.getNavUrlForRouterPopup(this.router)], { replaceUrl: true });
    }

    save() {
        this.isSaving = true;
        if (this.user.id) {
            this.userService.update(this.user).subscribe(
                (response) => this.onSaveSuccess(response),
                () => this.onSaveError()
            );
        } else {
            this.user.langKey = this.translateService.currentLang;
            this.userService.create(this.user).subscribe(
                (response) => this.onSaveSuccess(response),
                () => this.onSaveError());
        }
    }

    private onSaveSuccess(result) {
        this.eventManager.broadcast({ name: 'userListModification', content: 'OK' });
        this.isSaving = false;
        this.activeModal.dismiss(result);
        this.router.navigate([RouterHelper.getNavUrlForRouterPopup(this.router)], { replaceUrl: true });
    }

    private onSaveError() {
        this.isSaving = false;
    }
}

@Component({
    selector: 'pdm-user-dialog',
    template: ''
})
export class UserDialogComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor (
        private route: ActivatedRoute,
        private userModalService: UserModalService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe(params => {
            if ( params['login'] ) {
                this.modalRef = this.userModalService.open(UserMgmtDialogComponent, params['login']);
            } else {
                this.modalRef = this.userModalService.open(UserMgmtDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
