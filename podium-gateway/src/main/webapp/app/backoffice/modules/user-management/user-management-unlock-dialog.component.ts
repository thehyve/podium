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
import { JhiEventManager } from 'ng-jhipster';
import { UserService } from '../../../shared/user/user.service';
import { User } from '../../../shared/user/user.model';
import { UserModalService } from './user-modal.service';

@Component({
    selector: 'pdm-user-mgmt-unlock-dialog',
    templateUrl: './user-management-unlock-dialog.component.html'
})
export class UserMgmtUnlockDialogComponent {

    user: User;

    constructor(
        private userService: UserService,
        public activeModal: NgbActiveModal,
        private eventManager: JhiEventManager,
        private router: Router
    ) {

    }

    clear () {
        this.activeModal.dismiss('cancel');
        this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
    }

    confirmUnlock (user) {
        this.userService.unlock(user).subscribe(response => {
            this.eventManager.broadcast({ name: 'userListModification',
                content: 'Unlock a user'});
            this.activeModal.dismiss(true);
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
        });
    }

}

@Component({
    selector: 'pdm-user-unlock-dialog',
    template: ''
})
export class UserUnlockDialogComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor (
        private route: ActivatedRoute,
        private userModalService: UserModalService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe(params => {
            this.modalRef = this.userModalService.open(UserMgmtUnlockDialogComponent, params['login']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
