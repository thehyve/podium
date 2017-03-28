/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

export class Organisation {

    id?: number;
    uuid?: string;
    name?: string;
    shortName?: string;
    activated?: boolean;
    organisationUuid?: string;

    constructor(
        id?: number,
        uuid?: string,
        name?: string,
        shortName?: string,
        activated?: boolean,
        organisationUuid?: string
    ) {
        this.id = id ? id : null;
        this.uuid = uuid ? uuid : null;
        this.name = name ? name : null;
        this.shortName = shortName ? shortName : null;
        this.activated = activated ? activated : null;
        this.organisationUuid = organisationUuid ? organisationUuid : null;
    }
}
