/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';

import { PdmHealthService } from './health.service';
import { PdmHealthModalComponent } from './health-modal.component';

@Component({
    selector: 'pdm-health',
    templateUrl: './health.component.html',
})
export class PdmHealthCheckComponent implements OnInit {
    healthData: any;
    updatingHealth: boolean;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private modalService: NgbModal,
        private healthService: PdmHealthService
    ) {
        this.jhiLanguageService.setLocations(['health']);

    }

    ngOnInit() {
        this.refresh();
    }

    baseName(name: string) {
        return this.healthService.getBaseName(name);
    }

    getTagClass(statusState) {
        if (statusState === 'UP') {
            return 'tag-success';
        } else {
            return 'tag-danger';
        }
    }

    refresh() {
        this.updatingHealth = true;

        this.healthService.checkHealth().subscribe(health => {
            this.healthData = this.healthService.transformHealthData(health);
            this.updatingHealth = false;
        });
    }

    showHealth(health: any) {
        const modalRef  = this.modalService.open(PdmHealthModalComponent);
        modalRef.componentInstance.currentHealth = health;
        modalRef.result.then((result) => {
            console.log(`Closed with: ${result}`);
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
        });
    }

    subSystemName(name: string) {
        return this.healthService.getSubSystemName(name);
    }

}
