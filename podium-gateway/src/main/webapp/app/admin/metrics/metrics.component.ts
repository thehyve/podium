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
import { PdmMetricsMonitoringModalComponent } from './metrics-modal.component';
import { PdmMetricsService } from './metrics.service';

@Component({
    selector: 'pdm-metrics',
    templateUrl: './metrics.component.html',
})
export class PdmMetricsMonitoringComponent implements OnInit {
    metrics: any = {};
    cachesStats: any = {};
    servicesStats: any = {};
    updatingMetrics: boolean = true;
    JCACHE_KEY: string ;

    constructor(
        private jhiLanguageService: JhiLanguageService,
        private modalService: NgbModal,
        private metricsService: PdmMetricsService
    ) {
        this.JCACHE_KEY = 'jcache.statistics';
        this.jhiLanguageService.setLocations(['metrics']);
    }

    ngOnInit() {
        this.refresh();
    }

    refresh () {
        this.updatingMetrics = true;
        this.metricsService.getMetrics().subscribe((metrics) => {
            this.metrics = metrics;
            this.updatingMetrics = false;
            this.servicesStats = {};
            this.cachesStats = {};
            Object.keys(metrics.timers).forEach((key) => {
                let value = metrics.timers[key];
                if (key.indexOf('web.rest') !== -1 || key.indexOf('service') !== -1) {
                    this.servicesStats[key] = value;
                }
            });
            Object.keys(metrics.gauges).forEach((key) => {
                if (key.indexOf('jcache.statistics') !== -1) {
                    let value = metrics.gauges[key].value;
                    // remove gets or puts
                    let index = key.lastIndexOf('.');
                    let newKey = key.substr(0, index);

                    // Keep the name of the domain
                    this.cachesStats[newKey] = {
                        'name': this.JCACHE_KEY.length,
                        'value': value
                    };
                }
            });
        });
    }

    refreshThreadDumpData () {
        this.metricsService.threadDump().subscribe((data) => {
            const modalRef  = this.modalService.open(PdmMetricsMonitoringModalComponent, { size: 'lg'});
            modalRef.componentInstance.threadDump = data;
            modalRef.result.then((result) => {
                console.log(`Closed with: ${result}`);
            }, (reason) => {
                console.log(`Dismissed ${reason}`);
            });
        });
    }

}
