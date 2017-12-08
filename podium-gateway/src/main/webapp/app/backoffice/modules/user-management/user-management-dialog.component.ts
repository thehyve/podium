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
import { JhiEventManager, JhiLanguageService } from 'ng-jhipster';
import { UserModalService } from './user-modal.service';
import { JhiLanguageHelper, User, UserService } from '../../../shared';
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
        private languageHelper: JhiLanguageHelper,
        private languageService: JhiLanguageService,
        private userService: UserService,
        private JhiEventManager: JhiEventManager,
        private router: Router
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_RESEARCHER'];
        this.languageHelper.getAll().then((languages) => {
            this.languages = languages;
        });

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
            this.languageService.getCurrent().then((key) => {
                this.user.langKey = key;
                this.userService.create(this.user).subscribe(
                    (response) => this.onSaveSuccess(response),
                    () => this.onSaveError());
            });
        }
    }

    private onSaveSuccess(result) {
        this.JhiEventManager.broadcast({ name: 'userListModification', content: 'OK' });
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
