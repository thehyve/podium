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
import { Principal } from '../auth/principal.service';
import { User } from '../user/user.model';
import { RequestBase } from './request-base';
import { Organisation } from '../../backoffice/modules/organisation/organisation.model';
import { Authority } from '../authority/authority';
import { OrganisationAuthorityOptions, ORGANISATION_AUTHORITIES_MAP } from '../authority/authority.constants';

@Injectable()
export class RequestAccessService {

    private currentUser: User;
    private organisationAuthoritiesMap: { [token: string]: Authority; };

    constructor(
        private principal: Principal
    ) {
        this.principal.identity().then((account: User) => {
            this.currentUser = account;
            console.log('Account: ', account);
        });

        this.organisationAuthoritiesMap = ORGANISATION_AUTHORITIES_MAP;
        console.log('lalafds ', this.organisationAuthoritiesMap);
    }

    /**
     * Check whether the signed in user has the organisation coordinator permission in any of the organisations involved
     * with this request.
     * @param request the request to check against
     * @returns {boolean} true if the user has the permission; else false.
     */
    public isCoordinatorFor(request: RequestBase): boolean {
        if (!this.currentUser || !request) {
            return false;
        }

        let requiredAuthority = OrganisationAuthorityOptions.ROLE_ORGANISATION_COORDINATOR;
        let requiredPermission = OrganisationAuthorityOptions[requiredAuthority];
        return this.hasPermissionInAnyOrganisation(request, requiredPermission);
    }

    /**
     * Check whether the signed in user holds the organisation reviewer permission in any of the organisations involved
     * with this request.
     * @param request the request to check against
     * @returns {boolean} true if the user has the permission; else false.
     */
    public isReviewerFor(request: RequestBase): boolean {
        if (!this.currentUser || !request) {
            return false;
        }

        let requiredAuthority = OrganisationAuthorityOptions.ROLE_ORGANISATION_REVIEWER;
        let requiredPermission = OrganisationAuthorityOptions[requiredAuthority];
        return this.hasPermissionInAnyOrganisation(request, requiredPermission);
    }

    /**
     * Check whether the signed in user is the requester of this request.
     * @param request the request to check against
     * @returns {boolean} true if the user is the requester
     */
    public isRequesterOf(request: RequestBase): boolean {
        if (!this.currentUser || !request) {
            return false;
        }

        return this.currentUser.uuid === request.requester;
    }

    private hasPermissionInAnyOrganisation(request: RequestBase, requiredPermission: string): boolean {
        let organisations = request.organisations;
        // Filter involved organisations in the request for required permission.
        let permittedOrganisations = organisations.filter(
            organisation => {
                let userOrganisations = this.currentUser.organisationAuthorities[organisation.uuid] || [];
                return userOrganisations.indexOf(requiredPermission) > -1;
            });

        return permittedOrganisations.length > 0;
    }


}
