/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */


import { RequestBase } from '../request/request-base';
import { AttachmentTypes } from './attachment.constants';
import { User } from '../user/user.model';

export class Attachment {
    id: number;
    uuid: string;
    owner: User;
    request: RequestBase;
    createdDate: Date;
    lastModifiedDate: Date;
    fileByteSize: number;
    requestFileType: AttachmentTypes;
    fileName: string;
}
