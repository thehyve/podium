/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

// Adopted from https://github.com/emilol/angular2-crumbs/
import { Injectable, EventEmitter } from '@angular/core';
import { Router, RoutesRecognized, ActivatedRouteSnapshot } from '@angular/router';

export class Breadcrumb {
    displayName?: string;
    terminal: boolean;
    url: string;
}

@Injectable()
export class BreadcrumbService {
    onBreadcrumbChange: EventEmitter<Breadcrumb[]> = new EventEmitter<Breadcrumb[]>(false);

    private breadcrumbs: Breadcrumb[];

    constructor(private router: Router) {
        /**
         * Subscribe to all navigated route events
         * A breadcrumb is created for every firstChild of a recognisedRoute
         * and appended to the breadcrumb array
         */
        this.router.events.subscribe((routeEvent: RoutesRecognized) => {
            if (!(routeEvent instanceof RoutesRecognized)) {
                return;
            }

            let route = routeEvent.state.root;
            let url = '';

            this.breadcrumbs = [];

            // Root breadcrumb
            let rootBreadcrumb: Breadcrumb = {
                displayName: 'podium',
                terminal: false,
                url: '/'
            };

            this.breadcrumbs.push(rootBreadcrumb);

            while (route.children.length) {
                route = route.firstChild;
                if (!route.routeConfig.path) {
                    continue;
                }

                url += `/${this.createUrl(route)}`;

                this.breadcrumbs.push(this.createBreadcrumb(route, url));
            }

            this.onBreadcrumbChange.emit(this.breadcrumbs);
        });
    }

    public changeBreadcrumb(route: ActivatedRouteSnapshot, name: string) {
        let rootUrl = this.createRootUrl(route);
        let breadcrumb = this.breadcrumbs.find(bc => bc.url === rootUrl);

        breadcrumb.displayName = name;

        this.onBreadcrumbChange.emit(this.breadcrumbs);
    }

    private createBreadcrumb(route: ActivatedRouteSnapshot, url: string): Breadcrumb {
        return {
            displayName: route.data['breadcrumb'] || 'No breadcrumb',
            terminal: route.children.length === 0 || !route.firstChild.routeConfig.path,
            url: url
        };
    }

    private createUrl(route: ActivatedRouteSnapshot) {
        return route.url.map(s => s.toString()).join('/');
    }

    private createRootUrl(route: ActivatedRouteSnapshot) {
        let url = '';
        let next = route.root;

        while (next.firstChild !== route) {
            next = next.firstChild;
            if (!next.routeConfig.path) {
                continue;
            }

            url += `/${this.createUrl(next)}`;
        }

        url.concat(`/${this.createUrl(route)}`);

        return url;
    }
}
