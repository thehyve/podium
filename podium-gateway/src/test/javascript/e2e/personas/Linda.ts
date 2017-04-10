/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class Linda {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "Linda";
        this.properties = {
            "login": "linda",
            "password": "2piYJ4G{MokC",
            "firstName": "Linda",
            "lastName": "New bee",
            "email": "linda@localhost",
            "telephone": "0123456789",
            "institute": "Radio FM",
            "department": "AB-120",
            "jobTitle": "chief Radiological officer",
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

export = new Linda();
