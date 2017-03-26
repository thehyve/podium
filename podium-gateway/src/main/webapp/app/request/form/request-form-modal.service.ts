/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

import { Injectable, Component } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { User, UserService } from '../../shared';
import {RequestService} from '../../shared/request/request.service';
import {RequestBase} from '../../shared/request/request-base';

@Injectable()
export class RequestFormModalService {
    private isOpen = false;
    constructor (
        private modalService: NgbModal,
        private requestService: RequestService
    ) {}

    open (component: Component, requestUuid?: string): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (requestUuid) {
            this.requestService.findDraftByUuid(requestUuid).subscribe(request =>
                this.requestFormModalRef(component, request));
        } else {
            return this.requestFormModalRef(component, new RequestBase());
        }
    }

    requestFormModalRef(component: Component, request: RequestBase): NgbModalRef {
        let modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.request = request;
        modalRef.result.then(result => {
            console.log(`Closed with: ${result}`);
            this.isOpen = false;
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
            this.isOpen = false;
        });
        return modalRef;
    }
}
