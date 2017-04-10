/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class Admin {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "Admin";
        this.properties = {
            "userName": "admin",
            "password": "admin",
            "firstName": "Administrator",
            "lastName": "Administrator",
            "email": "admin@localhost",
            "telephone": null,
            "institute": null,
            "department": "admin@localhost",
            "jobTitle": null,
            "specialism": null,
            "Other specialism": null,
            "authority": [{
                orgShortName: "none",
                role: "ROLE_PODIUM_ADMIN"
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }
    }
}

export = new Admin();
