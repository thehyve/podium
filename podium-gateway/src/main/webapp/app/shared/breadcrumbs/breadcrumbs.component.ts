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
import { Breadcrumb, BreadcrumbService } from './breadcrumbs.service';
import { Principal } from '../auth/principal.service';

@Component({
    selector: 'pdm-breadcrumb',
    template:
        `<div  *ngIf="isAuthenticated()">
            <div #template>
                <ng-content></ng-content>
            </div>
            <div class="container" *ngIf="template.children.length == 0">
                <div class="nav-wrapper">
                    <div class="breadcrumb" *ngFor="let route of breadcrumbs" [ngClass]="{'last': route.terminal}">
                        <!-- disable link of last item -->
                        <a href="" *ngIf="!route.terminal" [routerLink]="[route.url]">{{ route.displayName }}</a>
                        <span *ngIf="route.terminal">{{ route.displayName }}</span>
                    </div>
                </div>
            </div>
        </div>`
})
export class BreadcrumbComponent {
    breadcrumbs: Breadcrumb[];

    constructor(
        private breadcrumbService: BreadcrumbService,
        private principal: Principal
    ) {
        breadcrumbService.onBreadcrumbChange.subscribe((crumbs: Breadcrumb[]) => {
            this.breadcrumbs = crumbs;
        });
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }
}
