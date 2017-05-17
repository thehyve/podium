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
    public previousAuthority: string;
    public authority: string = undefined;
    public isSaved: boolean;
    public isDirty: boolean = false;
    public searchTerm: string;
    public dataSource: Observable<any>;

    constructor() {
    }

}
