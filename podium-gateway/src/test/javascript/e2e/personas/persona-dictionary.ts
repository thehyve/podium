/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { Persona } from './templates';

/*
 * pages must be added to this dictionary for the director class to find them.
 */
function initPersonaDictionary() {
    let personaDictionary: { [key: string]: Persona } = Object.create(null);

    let personas = [
        new Persona('System', {
            "login": "system",
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
            "authority": [{
                orgShortName: "none",
                role: "ROLE_PODIUM_ADMIN"
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }),
        new Persona("Admin", {
            "login": "admin",
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
        }),
        new Persona("BBMRI_Admin", {
            "login": "bbmri_admin",
            "password": "bbmri_admin123!",
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
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }),
        new Persona('Linda', {
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
        }),
        new Persona('Dave', {
            "login": "dave",
            "password": "2piYJ4G{MokC",
            "firstName": "Dave",
            "lastName": "New bee",
            "email": "qqq@localhost",
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
        }),
        new Persona('Simone', {
            "login": "simone",
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
        }),
        new Persona('VarnameBank_Admin', {
            "login": "varnamebank_admin",
            "password": "varnamebank_admin123!",
            "firstName": "VarnameBank",
            "lastName": "Admin",
            "email": "VarnameBank_Admin@localhost",
            "telephone": "0123456789",
            "institute": "some bio thing",
            "department": "AB-100",
            "jobTitle": "chief researcher",
            "specialism": "Other",
            "Other specialism": "Other specialism",
            "authority": [{
                orgShortName: "VarnameBank",
                role: "ROLE_ORGANISATION_ADMIN"
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }),
        new Persona('Databank_Coordinator', {
            "login": "databank-coordinator",
            "password": "coordinator123!",
            "firstName": "coordinator",
            "lastName": "multi",
            "email": "databank-coordinator@localhost",
            "telephone": "0123456789",
            "institute": "some bio thing",
            "department": "AB-100",
            "jobTitle": "chief coordinator",
            "specialism": "Other",
            "Other specialism": "some specialism",
            "authority": [{
                orgShortName: "DataBank",
                role: "ROLE_ORGANISATION_COORDINATOR"
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }),
        new Persona('Request_Coordinator', {
            "login": "coordinator",
            "password": "coordinator123!",
            "firstName": "coordinator",
            "lastName": "multi",
            "email": "coordinator@localhost",
            "telephone": "0123456789",
            "institute": "some bio thing",
            "department": "AB-100",
            "jobTitle": "chief coordinator",
            "specialism": "Other",
            "Other specialism": "some specialism",
            "authority": [{
                orgShortName: "DataBank",
                role: "ROLE_ORGANISATION_COORDINATOR"
            }, {
                orgShortName: "ImageBank",
                role: "ROLE_ORGANISATION_COORDINATOR"
            }, {
                orgShortName: "BioBank",
                role: "ROLE_ORGANISATION_COORDINATOR"
            }, {
                orgShortName: "MultiBank",
                role: "ROLE_ORGANISATION_COORDINATOR"
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }),
        new Persona('Request_Reviewer', {
            "login": "Reviewer",
            "password": "Reviewer123!",
            "firstName": "Reviewer",
            "lastName": "multi",
            "email": "Reviewer@localhost",
            "telephone": "0123456789",
            "institute": "some bio thing",
            "department": "AB-100",
            "jobTitle": "chief Reviewer",
            "specialism": "Other",
            "Other specialism": "some specialism",
            "authority": [{
                orgShortName: "DataBank",
                role: "ROLE_REVIEWER"
            }, {
                orgShortName: "ImageBank",
                role: "ROLE_REVIEWER"
            }, {
                orgShortName: "BioBank",
                role: "ROLE_REVIEWER"
            }, {
                orgShortName: "MultiBank",
                role: "ROLE_REVIEWER"
            }],
            "emailVerified": true,
            "adminVerified": true,
            "accountLocked": false
        }),
        new Persona('blank user', {
            "login": "blank-user",
            "password": "2piYJ4G{MokC",
            "firstName": "blank",
            "lastName": "user",
            "email": "blank_user@localhost",
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
        }),
    ];

    personas.forEach((persona) => {
        persona = preprocess(persona);
        personaDictionary[persona.personaID] = persona;
    });

    return personaDictionary;
}

function preprocess(persona: Persona): Persona {
    persona["login"] = persona["login"].toLowerCase();
    persona["email"] = persona["email"].toLowerCase();
    return persona;
}

export = initPersonaDictionary;
