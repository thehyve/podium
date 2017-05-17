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
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'pdm-metrics-modal',
    templateUrl: './metrics-modal.component.html'
})
export class PdmMetricsMonitoringModalComponent implements OnInit {

    threadDumpFilter: any;
    threadDump: any;
    threadDumpAll: number = 0;
    threadDumpBlocked: number = 0;
    threadDumpRunnable: number = 0;
    threadDumpTimedWaiting: number = 0;
    threadDumpWaiting: number = 0;

    constructor(public activeModal: NgbActiveModal) {}

    ngOnInit() {
        this.threadDump.forEach((value) => {
            if (value.threadState === 'RUNNABLE') {
                this.threadDumpRunnable += 1;
            } else if (value.threadState === 'WAITING') {
                this.threadDumpWaiting += 1;
            } else if (value.threadState === 'TIMED_WAITING') {
                this.threadDumpTimedWaiting += 1;
            } else if (value.threadState === 'BLOCKED') {
                this.threadDumpBlocked += 1;
            }
        });

        this.threadDumpAll = this.threadDumpRunnable + this.threadDumpWaiting +
            this.threadDumpTimedWaiting + this.threadDumpBlocked;
    }

    getTagClass (threadState) {
        if (threadState === 'RUNNABLE') {
            return 'tag-success';
        } else if (threadState === 'WAITING') {
            return 'tag-info';
        } else if (threadState === 'TIMED_WAITING') {
            return 'tag-warning';
        } else if (threadState === 'BLOCKED') {
            return 'tag-danger';
        }
    }
}
