/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

/*
 * Should adhere to the Persona interface.
 * A persona class must have a name and should hold any properties that make writing test scripts easier/maintainable.
 */
class AdminPersona {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "'THE' admin";
        this.properties = {
            "userName": "admin",
            "password": "admin",
        }
    }
}

export = new AdminPersona();
