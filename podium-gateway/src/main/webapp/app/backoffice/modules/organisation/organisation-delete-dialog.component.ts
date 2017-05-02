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
import { EventManager, JhiLanguageService } from 'ng-jhipster';
import { Organisation } from './organisation.model';
import { OrganisationPopupService } from './organisation-popup.service';
import { OrganisationService } from './organisation.service';

@Component({
    selector: 'jhi-organisation-delete-dialog',
    templateUrl: './organisation-delete-dialog.component.html'
})
export class OrganisationDeleteDialogComponent {

    organisation: Organisation;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private organisationService: OrganisationService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager,
        private router: Router
    ) {
        this.jhiLanguageService.setLocations(['organisation']);
    }

    clear () {
        this.activeModal.dismiss('cancel');
        this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
    }

    confirmDelete (id: number) {
        this.organisationService.delete(id).subscribe(response => {
            this.eventManager.broadcast({
                name: 'organisationListModification',
                content: 'Deleted an organisation'
            });
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-organisation-delete-popup',
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
