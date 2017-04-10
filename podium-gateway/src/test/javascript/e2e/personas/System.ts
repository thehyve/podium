/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

class System {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "Dave";
        this.properties = {
            "userName": "system",
            "password": "",
            "firstName": "System",
            "lastName": "System",
            "email": "system@localhost",
            "telephone": null,
            "institute": null,
            "department": "system@localhost",
            "jobTitle": null,
            "specialism": null,
            "Other specialism": null,
            "authority": ["ROLE_PODIUM_ADMIN"],
            "emailVerified": "Email verified",
            "adminVerified": "Account verified",
        }
    }
}

export = new System();
