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
import { Router, ActivatedRouteSnapshot, NavigationEnd, RoutesRecognized } from '@angular/router';

import { BreadcrumbService } from 'ng2-breadcrumb/ng2-breadcrumb';

import { JhiLanguageHelper, StateStorageService } from '../../shared';
import { Principal } from '../../shared/auth/principal.service';

@Component({
    selector: 'podium-main',
    templateUrl: './main.component.html'
})
export class PdmMainComponent implements OnInit {

    constructor(
        private jhiLanguageHelper: JhiLanguageHelper,
        private router: Router,
        private breadcrumbService: BreadcrumbService,
        private principal: Principal
    ) { }

    private getPageTitle(routeSnapshot: ActivatedRouteSnapshot) {
        let title: string = (routeSnapshot.data && routeSnapshot.data['pageTitle'])
            ? routeSnapshot.data['pageTitle']
            : 'BBMRI Podium Request Portal';
        if (routeSnapshot.firstChild) {
            title = this.getPageTitle(routeSnapshot.firstChild) || title;
        }
        return title;
    }

    ngOnInit() {
        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.jhiLanguageHelper.updateTitle(this.getPageTitle(this.router.routerState.snapshot.root));
            }
        });

        this.configureBreadcrumb();
    }

    /**
     * Configure friendly names for routes
     */
    configureBreadcrumb () {
        this.breadcrumbService.addFriendlyNameForRoute('/', 'Home');
        this.breadcrumbService.addFriendlyNameForRoute('/dashboard', 'Dashboard');
        this.breadcrumbService.addFriendlyNameForRoute('/organisation', 'Dashboard');
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }
}
