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
        this.name = "Admin";
        this.properties = {
            "userName": "bbmri_admin",
            "password": "bbmri_admin",
            "firstName": "BBMRI administrator",
            "lastName": "BBMRI administrator",
            "email": "bbmri_admin@localhost",
            "telephone": null,
            "institute": null,
            "department": "bbmri_admin@localhost",
            "jobTitle": null,
            "specialism": null,
            "Other specialism": null,
            "authority": ["ROLE_PODIUM_ADMIN", "ROLE_BBMRI_ADMIN"],
            "emailVerified": "Email verified",
            "adminVerified": "Account verified",
        }
    }
}

export = new BBMRI_Admin();
