/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import request = require('request-promise-native')

import { isUndefined } from 'util';
import { browser } from 'protractor';
import { Persona } from './director';

export class AdminConsole {
    public token: string;

    constructor() {
    }

    public authenticate(persona?: Persona) {//store token first time
        let login;
        let password;

        if (!isUndefined(persona)) {
            login = persona.properties["login"];
            password = persona.properties["password"];
        } else {
            login = "admin";
            password = "admin";
        }

        let options = {
            method: 'POST',
            url: browser.baseUrl + 'podiumuaa/oauth/token',
            headers: {
                'Authorization': 'Basic d2ViX2FwcDo='
            },
            form: {
                grant_type: "password",
                username: login,
                password: password
            }
        };
        return request(options)
    }

    public checkUser(persona, check) {
        return this.authenticate().then((body) => {
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'podiumuaa/api/users/' + persona.properties['login'],
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };

            return request(options).then((body) => {
                    let user = parseJSON(body);

                    if (!check(persona, user)) {
                        console.log("http checkUser " + persona.properties['login']);
                        return "checkUser failed";
                    }
                }, (reason) => {
                    let user = parseJSON(reason['error']);

                    if (!check(persona, user)) {
                        console.log("http checkUser " + persona.properties['login'] + " " + reason["statusCode"]);
                        return "checkUser failed";
                    }
                }
            )
        });
    }

    public unlockUser(persona) {
        let token;

        this.authenticate().then((body) => {
            token = parseJSON(body).access_token;
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'podiumuaa/api/users/' + persona.properties['login'],
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            };
            return request(options).then((body) => {

                let user = parseJSON(body);

                let options = {
                    method: 'PUT',
                    url: browser.baseUrl + 'podiumuaa/api/users/uuid/' + user.uuid + '/unlock',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                };
                return request(options).then((body), (reason) => {
                    console.log("http unlockUser " + persona.properties['login'] + " " + reason["statusCode"], body);
                    return "unlockUser failed"
                })
            })
        })
    }

    public checkorganisation(expectedorganisation, check) {

        return this.authenticate().then((body) => {
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'podiumuaa/api/organisations/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                let organisations = parseJSON(body);
                let organisation = organisations.filter(function (value) {
                    return value["shortName"] == expectedorganisation.properties["shortName"];
                })[0];

                if (!check(expectedorganisation, organisation)) {
                    return JSON.stringify(organisation) + " did not match for " + JSON.stringify(expectedorganisation)
                }

            })
        });
    }

    public checkDraft(expectedDraft, check, user) {

        return this.authenticate(user).then((body) => {
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'api/requests/drafts',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                let drafts = parseJSON(body);

                let draft = drafts.filter(function (value) {
                    return value["requestDetail"]["title"] == expectedDraft.properties["title"];
                })[0];

                if (!check(expectedDraft, draft)) {
                    return JSON.stringify(draft) + " did not match for " + JSON.stringify(expectedDraft)
                }
            })
        })
    }

    public createUser(persona: Persona) {
        return this.authenticate().then((body) => {
            let options = {
                method: 'POST',
                url: browser.baseUrl + 'podiumuaa/api/test/users',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(
                    {
                        "id": null,
                        "login": persona.properties['login'],
                        "firstName": persona.properties['firstName'],
                        "lastName": persona.properties['lastName'],
                        "email": persona.properties['email'],
                        "telephone": persona.properties['telephone'],
                        "institute": persona.properties['institute'],
                        "department": persona.properties['department'],
                        "jobTitle": persona.properties['jobTitle'],
                        "specialism": persona.properties['specialism'],
                        "emailVerified": persona.properties['emailVerified'],
                        "adminVerified": persona.properties['adminVerified'],
                        "accountLocked": persona.properties['accountLocked'],
                        "langKey": "en",
                        "createdBy": null,
                        "createdDate": null,
                        "lastModifiedBy": null,
                        "lastModifiedDate": null,
                        "password": persona.properties['password']
                    }
                )
            };

            return request(options).catch((reason) => {
                console.log("http createUser " + persona.properties['login'], reason["statusCode"]);
                return "createUser failed"
            });
        });
    }

    public createorganisation(organisation: any) {
        return this.authenticate().then((body) => {
            let options = {
                method: 'POST',
                url: browser.baseUrl + 'podiumuaa/api/organisations/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(
                    {
                        "name": organisation.properties['name'],
                        "shortName": organisation.properties['shortName'],
                        "activated": true
                    }
                )
            };
            return request(options).catch((reason) => {
                    console.log("http createorganisation " + organisation.properties['shortName'] + " " + reason["statusCode"]);
                    return "createorganisation failed"
                }
            )
        });
    }

    public createRequest(Request: any) {
        let that = this;
        return new Promise(function (resolve, reject) {
            if (false) {
                // resolve()
            } else {
                console.log("creating a Request is not implemented yet");
                reject("createRequest failed")
            }
        });
    }

    public cleanDB() {
        return this.authenticate().then((body) => {
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'podiumuaa/api/test/clearDatabase',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                },
            };
            return request(options).catch((reason) => {
                console.log("cleanDB returned: " + reason["statusCode"], body);
                return "cleanDB failed";
            })
        });
    }

    assignRole(orgShortName: string, role: string, users: [string]) {
        return this.authenticate().then((body) => {
            let options = {
                method: 'POST',
                url: browser.baseUrl + 'podiumuaa/api/test/roles/assign',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(
                    {
                        "organisation": orgShortName,
                        "authority": role,
                        "users": users
                    }
                )
            };
            return request(options).catch((reason) => {
                    console.log("http assignRole organisation", orgShortName, role, users, reason["statusCode"], body);
                    return "assignRole failed";
                }
            )
        });
    }

    getOrgUUID(orgShortName: string) {
        return this.authenticate().then((body) => {
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'podiumuaa/api/organisations/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };

            return request(options).then((body) => {
                let organisations = parseJSON(body);
                let organisation = organisations.filter(function (value) {
                    return value["shortName"] == orgShortName;
                })[0];
                return organisation["uuid"] as string;
            }).catch((reason) => {
                console.log("http getOrgUUID", orgShortName, reason["statusCode"], body);
                return "getOrgUUID failed";
            })
        });
    }
}

function parseJSON(string: string) {
    if (string == '') {
        return string
    }
    return JSON.parse(string);
}
