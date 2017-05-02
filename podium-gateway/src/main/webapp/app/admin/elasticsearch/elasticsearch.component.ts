/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiLanguageService } from 'ng-jhipster';
import { PdmElasticsearchModalComponent } from './elasticsearch-modal.component';

@Component({
    selector: 'pdm-elasticsearch',
    templateUrl: './elasticsearch.component.html',
})
export class PdmElasticsearchComponent {
    processingIndex: boolean;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private modalService: NgbModal
    ) {
        this.jhiLanguageService.setLocations(['elasticsearch']);
    }

    confirmReindexing() {
        const modalRef  = this.modalService.open(PdmElasticsearchModalComponent);
        modalRef.result.then((result) => {
            this.processingIndex = result;
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
        });
    }

}
