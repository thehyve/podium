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
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PdmElasticsearchService } from './elasticsearch.service';

@Component({
    selector: 'pdm-elasticsearch-modal',
    templateUrl: './elasticsearch-modal.component.html'
})
export class PdmElasticsearchModalComponent {

    isReindexing: boolean;

    constructor(
        private elasticsearchService: PdmElasticsearchService,
        public activeModal: NgbActiveModal) {
    }

    confirmReindex() {
        this.isReindexing = true;
        this.elasticsearchService.reindex().subscribe((res) => {
            this.isReindexing = false;
            this.activeModal.close(res.ok);
        });
    }
}
