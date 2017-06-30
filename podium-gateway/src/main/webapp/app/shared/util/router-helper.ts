/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable()
export class RouterHelper {

    /**
     * Get base url for current route path
     * @param router the router
     * @returns {string} the base url
     */
    public static getNavUrlForRouter(router: Router) {
        return router.url.split(/\?/)[0].split(/;/)[0] + '/';
    }

    /**
     * Get base url for popup component route paths
     * @param router the router
     * @returns {string} the base url
     */
    public static getNavUrlForRouterPopup(router: Router) {
        return router.url.split(/\(/)[0];
    }
}
