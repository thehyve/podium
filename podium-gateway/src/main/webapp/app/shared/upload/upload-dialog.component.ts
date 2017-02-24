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
import { Response } from '@angular/http';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService, JhiLanguageService } from 'ng-jhipster';

import { UploadPopupService } from './upload-dialog.service';
import { FileUploader } from 'ng2-file-upload';

const URL = 'api/upload/';

@Component({
    selector: 'pdm-upload-dialog',
    templateUrl: './upload-dialog.component.html',
    styleUrls: ['upload.scss']
})
export class UploadDialogComponent implements OnInit {

    authorities: any[];
    isUploading: boolean;

    public hasDropzoneOver: boolean = false;
    public uploader: FileUploader = new FileUploader({ url: URL });

    constructor(
        public activeModal: NgbActiveModal,
        private jhiLanguageService: JhiLanguageService,
        private alertService: AlertService,
        private eventManager: EventManager,
        private router: Router
    ) {
        // this.jhiLanguageService.setLocations(['upload']);
    }

    ngOnInit() {
        // this.isSaving = false;
    }

    clear () {
        this.activeModal.dismiss('cancel');
        this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
    }

    public fileOverDropzone(e: any): void {
        this.hasDropzoneOver = e;
    }

}

@Component({
    selector: 'pdm-upload-popup',
    template: ''
})
export class UploadPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor (
        private route: ActivatedRoute,
        private uploadPopupService: UploadPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe(params => {
            /*if ( params['uuid'] ) {
                this.modalRef = this.organisationPopupService
                    .open(OrganisationDialogComponent, params['uuid']);
            } else {*/
                this.modalRef = this.uploadPopupService
                    .open(UploadDialogComponent);
            // }

        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
