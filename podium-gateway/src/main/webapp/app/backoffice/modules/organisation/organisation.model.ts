/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Role } from '../role/role.model';
import { RequestType } from '../../../shared/request/request-type';

export class Organisation {
    constructor(
        public id?: number,
        public uuid?: string,
        public name?: string,
        public shortName?: string,
        public roles?: Role[],
        public requestTypes?: RequestType[]
    ) { }

}
