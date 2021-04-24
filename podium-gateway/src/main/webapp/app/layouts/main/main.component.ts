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
import { Title } from '@angular/platform-browser';
import { Router, ActivatedRouteSnapshot, NavigationEnd } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { AccountService } from '../../core/auth/account.service';

@Component({
    selector: 'pdm-main',
    templateUrl: './main.component.html'
})
export class PdmMainComponent implements OnInit {

    constructor(
        private titleService: Title,
        private router: Router,
        private translateService: TranslateService,
        private accountService: AccountService
    ) { }

    private getPageTitle(routeSnapshot: ActivatedRouteSnapshot) {
        let title: string = (routeSnapshot.data && routeSnapshot.data['pageTitle'])
            ? routeSnapshot.data['pageTitle']
            : 'BBMRI Request Portal Podium';
        if (routeSnapshot.firstChild) {
            title = this.getPageTitle(routeSnapshot.firstChild) || title;
        }
        return title;
    }

    private updateTitle(): void {
        let pageTitle = this.getPageTitle(this.router.routerState.snapshot.root);
        if (!pageTitle) {
            pageTitle = 'global.title';
        }
        this.translateService.get(pageTitle).subscribe(title => this.titleService.setTitle(title));
    }

    ngOnInit() {
        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.updateTitle();
                // Scroll to top of page after page navigation
                window.scrollTo(0, 0);
            }
        });
    }

    isAuthenticated() {
        return this.accountService.isAuthenticated();
    }
}
