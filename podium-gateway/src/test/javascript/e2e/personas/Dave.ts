/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
class Dave {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "Dave";
        this.properties = {
            "userName": "newdave",
            "password": "2piYJ4G{MokC",
            "firstName": "Dave",
            "lastName": "New bee",
            "email": "dave@localhost",
            "telephone": "0123456789",
            "institute": "Radio FM",
            "department": "AB-120",
            "jobTitle": "chief Radiological officer",
            "specialism": "Other",
            "Other specialism": "Other specialism"
        }
    }
}

export = new Dave();
