/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import { Observable } from 'rxjs';

export class OrganisationUser {
    public id: string;
    public uuid: string;
    public fullName: string;
    public authority: string = undefined;
    public isSaved: boolean;
    public isDirty: boolean;
    public searchTerm: string;
    public dataSource: Observable<any>;

    constructor(
        id?: string,
        uuid?: string,
        fullName?: string,
        authority?: string,
        isSaved?: boolean,
        isDirty?: boolean,
        searchTerm?: string,
        dataSource?: Observable<any>
    ) {
        this.id = id ? id : null;
        this.uuid = uuid ? uuid : null;
        this.fullName = fullName ? fullName : null;
        this.authority = authority ? authority : undefined;
        this.isSaved = isSaved ? isSaved : null;
        this.isDirty = isDirty ? isDirty : null;
        this.searchTerm = searchTerm ? searchTerm : null;
        this.dataSource = dataSource ? dataSource : null;
    }
}
