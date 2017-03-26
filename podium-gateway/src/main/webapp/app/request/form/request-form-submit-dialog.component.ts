/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, JhiLanguageService } from 'ng-jhipster';

import {RequestService} from '../../shared/request/request.service';
import {RequestFormModalService} from './request-form-modal.service';
import {RequestBase} from '../../shared/request/request-base';

@Component({
    selector: 'request-form-submit-dialog',
    templateUrl: './request-form-submit-dialog.component.html'
})
export class RequestFormSubmitDialogComponent {

    request: RequestBase;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private requestService: RequestService,
        public activeModal: NgbActiveModal,
        private eventManager: EventManager,
        private router: Router
    ) {
        this.jhiLanguageService.setLocations(['request']);
    }

    clear () {
        this.activeModal.dismiss('cancel');
        this.router.navigate([{ outlets: { submit: null }}], { replaceUrl: true });
    }

    confirmSubmit (request: RequestBase) {
        this.requestService.submitDraft(request.uuid).subscribe(response => {
            this.eventManager.broadcast({ name: 'request',
                content: 'Submit a request'});
            this.activeModal.dismiss(true);
            this.router.navigate([{ outlets: { submit: null }}], { replaceUrl: true });
        });
    }

}

@Component({
    selector: 'request-form-submit-popup',
    template: ''
})
export class RequestFormSubmitPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor (
        private route: ActivatedRoute,
        private requestFormModalService: RequestFormModalService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe(params => {
            this.modalRef = this.requestFormModalService.open(RequestFormSubmitDialogComponent, params['uuid']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
