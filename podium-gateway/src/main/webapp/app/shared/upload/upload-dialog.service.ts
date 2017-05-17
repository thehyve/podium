/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Injectable, Component } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

@Injectable()
export class UploadPopupService {
    private isOpen = false;
    constructor (
        private modalService: NgbModal
    ) {}

    open (component: Component, uuid?: string | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        return this.uploadModalRef(component);

        /**
         * Load request by UUID
         */

        /**
         * Else throw error and close dialog on confirm
         */
    }

    uploadModalRef(component: Component): NgbModalRef {
        let modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.result.then(result => {
            this.isOpen = false;
        }, (reason) => {
            this.isOpen = false;
        });
        return modalRef;
    }
}
