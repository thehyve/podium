/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class Simone {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "Simone";
        this.properties = {
            "login": "researcher",
            "password": "2piYJ4G{MokC",
            "firstName": "Simone",
            "lastName": "hyve",
            "email": "simone@localhost",
            "telephone": "0123456789",
            "institute": "some bio thing",
            "department": "AB-100",
            "jobTitle": "chief researcher",
            "specialism": "Other",
            "Other specialism": "Other specialism",
            "authority": [{
                orgShortName: "none",
                role: "ROLE_RESEARCHER"
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }
    }
}

export = new Simone();
