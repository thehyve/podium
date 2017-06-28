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
import { OrganisationAuthorityOptions } from '../authority/authority.constants';
import { RequestStatusOptions, RequestReviewStatusOptions } from './request-status/request-status.constants';
import { RequestReviewFeedback } from './request-review-feedback';
import { RequestReviewDecision } from './request-review-decision';

@Injectable()
export class RequestAccessService {

    private currentUser: User;

    /**
     * Check whether a request has a specific status.
     * @param request the request
     * @param status the status
     * @returns {boolean} true if the request has the status
     */
    public static isRequestStatus(request: RequestBase, status: RequestStatusOptions): boolean {
        let requiredStatus = RequestStatusOptions[status];
        let requestStatus = request.status.toString();
        return requestStatus === requiredStatus;
    }

    /**
     * Check whether a request review has a specific status.
     * @param request the request
     * @param status the review status
     * @returns {boolean} true if the request review has the status
     */
    public static isRequestReviewStatus(request: RequestBase, reviewStatus: RequestReviewStatusOptions): boolean {
        let requiredStatus = RequestReviewStatusOptions[reviewStatus];
        let requestReview = request.requestReview;

        if (!requestReview) {
            return false;
        }

        let requestReviewStatus = requestReview.status.toString();
        return requestReviewStatus === requiredStatus;
    }

    constructor(
        private principal: Principal
    ) {
        this.loadCurrentUser(false);
    }

    public loadCurrentUser(force: boolean) {
        this.principal.identity(force).then((account: User) => {
            this.currentUser = account;
        });
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

        let requiredAuthority = OrganisationAuthorityOptions.ROLE_REVIEWER;
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

        return this.currentUser.uuid === request.requester.uuid;
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

    public isReviewable(lastFeedbacks: RequestReviewFeedback[]): boolean {
        let lastFeedback: RequestReviewFeedback;

        if (!this.currentUser || !lastFeedbacks) {
            return false;
        }

        // get current reviewer feedback
        lastFeedback = lastFeedbacks.find((feedback) => {
            return feedback.reviewer.uuid === this.currentUser.uuid;
        });

        if (lastFeedback) {
            // check if advice has not been given
            return lastFeedback.advice === RequestReviewDecision.None;
        } else {
            return false;
        }
    }
}
