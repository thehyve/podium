/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { Component, OnDestroy, OnInit } from '@angular/core';
import { AlertService } from '../../core/util/alert.service';


@Component({
    selector: 'pdm-alert',
    template: `
        <div class="alerts" role="alert">
            <div *ngFor="let alert of alerts" >
                <ngb-alert [type]="alert.type" (close)="alert.close(alerts)"><pre [innerHTML]="alert.msg"></pre></ngb-alert>
            </div>
        </div>`
})
export class PdmAlertComponent implements OnInit, OnDestroy {
    alerts: any[];

    constructor(private alertService: AlertService) { }

    ngOnInit() {
        this.alerts = this.alertService.get();
    }

    ngOnDestroy() {
        this.alerts = [];
    }
}
