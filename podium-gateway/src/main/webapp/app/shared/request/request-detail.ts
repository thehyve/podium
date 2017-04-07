/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { PrincipalInvestigator } from './principal-investigator';
import { RequestType } from './request-type';
import { Attachment } from '../attachment/attachment';

export class RequestDetail {

    principalInvestigator: PrincipalInvestigator = new PrincipalInvestigator();
    relatedRequest: string;

    id?: string;
    title: string;
    background: string;
    researchQuestion: string;
    hypothesis: string;
    methods: string;

    searchQuery: string;
    combinedRequest: boolean;
    requestType: RequestType[] = [];

    attachments: Attachment[];

    constructor() {
    }

}
