/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
export class User {

    public id?: any;
    public uuid?: string;
    public login?: string;
    public firstName?: string;
    public lastName?: string;
    public email?: string;
    public telephone?: string;
    public institute?: string;
    public department?: string;
    public jobTitle?: string;
    public specialism?: string;
    public emailVerified?: boolean;
    public adminVerified?: boolean;
    public accountLocked?: boolean;
    public langKey?: string;
    public authorities?: any[];
    public organisationAuthorities?: { [uuid: string]: string[] };
    public createdBy?: string;
    public createdDate?: Date;
    public lastModifiedBy?: string;
    public lastModifiedDate?: Date;
    public password?: string;

    constructor() {
    }

}
