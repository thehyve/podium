/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { AuditData } from './audit-data.model';

export class Audit {
    constructor(
        public data: AuditData,
        public principal: string,
        public timestamp: string,
        public type: string) {
    }
}
