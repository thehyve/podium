/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class BBMRI_Admin {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "bbmri_admin";
        this.properties = {
            "login": "bbmri_admin",
            "password": "bbmri_admin",
            "firstName": "BBMRI administrator",
            "lastName": "BBMRI administrator",
            "email": "bbmri_admin@localhost",
            "telephone": "06123456789",
            "institute": "BBMRI",
            "department": "bbmri_admin@localhost",
            "jobTitle": "bbmri_admin",
            "specialism": "other",
            "Other specialism": null,
            "authority": [{
                orgShortName: "none",
                role: "ROLE_BBMRI_ADMIN"
            }, {
                orgShortName: "none",
                role: "ROLE_RESEARCHER"
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }
    }
}

export = new BBMRI_Admin();
