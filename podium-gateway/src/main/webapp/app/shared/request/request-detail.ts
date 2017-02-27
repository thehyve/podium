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
    principalInvestigator: PrincipalInvestigator;
    relatedRequest: string;

    title: string;
    background: string;
    researchQuestion: string;
    hypothesis: string;
    methods: string;

    searchQuery: string;
    combinedRequest: boolean;
    requestType: RequestType[];

    attachments: Attachment[];

    constructor(
        principalInvestigator?: PrincipalInvestigator,
        relatedRequest?: string,
        title?: string,
        background?: string,
        researchQuestion?: string,
        hypothesis?: string,
        methods?: string,
        searchQuery?: string,
        combinedRequest?: boolean,
        requestType?: RequestType[],
        attachments?: Attachment[]
    ) {
          this.principalInvestigator
              = principalInvestigator ? principalInvestigator : new PrincipalInvestigator();
          this.relatedRequest = relatedRequest ? relatedRequest : null;
          this.title = title ? title : null;
          this.background = background ? background : null;
          this.researchQuestion = researchQuestion ? researchQuestion : null;
          this.hypothesis = hypothesis ? hypothesis : null;
          this.methods = methods ? methods : null;
          this.searchQuery = searchQuery ? searchQuery : null;
          this.combinedRequest = combinedRequest ? combinedRequest : null;
          this.requestType = requestType ? requestType : null;
          this.attachments = attachments ? attachments : null;
    }
}

