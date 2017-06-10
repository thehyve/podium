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
import { Organisation } from './organisation.model';
import { OrganisationService } from './organisation.service';
@Injectable()
export class OrganisationPopupService {
    private isOpen = false;
    constructor (
        private modalService: NgbModal,
        private organisationService: OrganisationService
    ) {}

    open (component: Component, uuid?: string | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (uuid) {
            this.organisationService.findByUuid(uuid).subscribe(organisation => {
                this.organisationModalRef(component, organisation);
            });
        } else {
            return this.organisationModalRef(component, new Organisation());
        }
    }

    organisationModalRef(component: Component, organisation: Organisation): NgbModalRef {
        let modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.organisation = organisation;
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
