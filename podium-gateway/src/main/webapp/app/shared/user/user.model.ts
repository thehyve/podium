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
    public emailVerified?: Boolean;
    public adminVerified?: Boolean;
    public accountLocked?: Boolean;
    public langKey?: string;
    public authorities?: any[];
    public createdBy?: string;
    public createdDate?: Date;
    public lastModifiedBy?: string;
    public lastModifiedDate?: Date;
    public password?: string;
    constructor(
        id?: any,
        uuid?: string,
        login?: string,
        firstName?: string,
        lastName?: string,
        email?: string,
        telephone?: string,
        institute?: string,
        department?: string,
        jobTitle?: string,
        specialism?: string,
        emailVerified?: Boolean,
        adminVerified?: Boolean,
        accountLocked?: Boolean,
        langKey?: string,
        authorities?: any[],
        createdBy?: string,
        createdDate?: Date,
        lastModifiedBy?: string,
        lastModifiedDate?: Date,
        password?: string
    ) {
        this.id = id ? id : null;
        this.uuid = uuid ? uuid : null;
        this.login = login ? login : null;
        this.firstName = firstName ? firstName : null;
        this.lastName = lastName ? lastName : null;
        this.email = email ? email : null;
        this.telephone = telephone ? telephone : null;
        this.institute = institute ? institute : null;
        this.department = department ? department : null;
        this.jobTitle = jobTitle ? jobTitle : null;
        this.specialism = specialism ? specialism : null;
        this.emailVerified = emailVerified ? emailVerified : false;
        this.adminVerified = adminVerified ? adminVerified : false;
        this.accountLocked = accountLocked ? accountLocked : false;
        this.langKey = langKey ? langKey : null;
        this.authorities = authorities ? authorities : null;
        this.createdBy = createdBy ? createdBy : null;
        this.createdDate = createdDate ? createdDate : null;
        this.lastModifiedBy = lastModifiedBy ? lastModifiedBy : null;
        this.lastModifiedDate = lastModifiedDate ? lastModifiedDate : null;
        this.password = password ? password : null;
    }
}
