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
import { EventManager } from '../../../core/util/event-manager.service';
import { OrganisationPopupService } from './organisation-popup.service';
import { Organisation } from '../../../shared/organisation/organisation.model';
import { OrganisationService } from '../../../shared/organisation/organisation.service';
import { RouterHelper } from '../../../shared/util/router-helper';

@Component({
    selector: 'pdm-organisation-delete-dialog',
    templateUrl: './organisation-delete-dialog.component.html'
})
export class OrganisationDeleteDialogComponent {

    organisation: Organisation;

    constructor(
        private organisationService: OrganisationService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager,
        private router: Router
    ) {

    }

    clear () {
        this.activeModal.dismiss('cancel');
        this.router.navigate([RouterHelper.getNavUrlForRouterPopup(this.router)], { replaceUrl: true });
    }

    confirmDelete (uuid: string) {
        this.organisationService.delete(uuid).subscribe(response => {
            this.eventManager.broadcast({
                name: 'organisationListModification',
                content: 'Deleted an organisation'
            });
            this.router.navigate([RouterHelper.getNavUrlForRouterPopup(this.router)], { replaceUrl: true });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'pdm-organisation-delete-popup',
    template: ''
})
export class OrganisationDeletePopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor (
        private route: ActivatedRoute,
        private organisationPopupService: OrganisationPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe(params => {
            this.modalRef = this.organisationPopupService
                .open(OrganisationDeleteDialogComponent, params['uuid']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
