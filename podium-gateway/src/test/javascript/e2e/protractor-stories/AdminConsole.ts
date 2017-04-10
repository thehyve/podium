/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import request = require('request')
import {isUndefined} from "util";
import {browser} from "protractor";
import {Persona} from "./director";

export class AdminConsole {
    public token: string;

    constructor() {
    }

    public authenticate(callback, persona?: Persona) {//store token first time
        let that = this;
        let username;
        let password;

        if (!isUndefined(persona)) {
            username = persona.properties["userName"];
            password = persona.properties["password"];
        } else {
            username = "admin";
            password = "admin";
        }

        let options = {
            method: 'POST',
            url: browser.baseUrl + 'podiumuaa/oauth/token',
            headers: {
                'Authorization': 'Basic d2ViX2FwcDo='
            },
            formData: {
                grant_type: "password",
                username: username,
                password: password
            }
        };

        request(options, callback)
    }


    public checkUser(persona, check, callback) {

        this.authenticate(function (error, response, body) {
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'podiumuaa/api/users/' + persona.properties.userName,
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            request(options, function (error, response, body) {
                let user = parseJSON(body);
                if (check(persona, user)) {
                    callback()
                } else {
                    callback("check failed")
                }
            })
        });
    }

    public deleteUser(persona, callback) {
        this.authenticate(function (error, response, body) {
            let options = {
                method: 'DELETE',
                url: browser.baseUrl + 'podiumuaa/api/users/' + persona.properties.userName,
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            request(options, function (error, response, body) {
                console.log(response);
                // callback();
            })
        });
    }

    public unlockUser(persona) {
        let token;
        this.authenticate(function (error, response, body) {
            token = parseJSON(body).access_token;
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'podiumuaa/api/users/' + persona.properties.userName,
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            };
            request(options, function (error, response, body) {

                let user = parseJSON(body);

                let options = {
                    method: 'PUT',
                    url: browser.baseUrl + 'podiumuaa/api/users/uuid/' + user.uuid + '/unlock',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                };
                request(options)
            })
        });
    }

    public checkOrganization(expectedOrganization, check, callback) {

        this.authenticate(function (error, response, body) {
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'podiumuaa/api/organisations/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            request(options, function (error, response, body) {
                let organizations = parseJSON(body);
                let organization = organizations.filter(function (value) {
                    return value["shortName"] == expectedOrganization.properties["shortName"];
                })[0];

                if (check(expectedOrganization, organization)) {
                    callback()
                } else {
                    callback(JSON.stringify(organization) + " did not match for " + JSON.stringify(expectedOrganization))
                }
            })
        });
    }

    public checkDraft(expectedDraft, check, callback, user) {

        this.authenticate(function (error, response, body) {
            let options = {
                method: 'GET',
                url: browser.baseUrl + 'api/requests/drafts',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            request(options, function (error, response, body) {
                let drafts = parseJSON(body);

                let draft = drafts.filter(function (value) {
                    return value["requestDetail"]["title"] == expectedDraft.properties["title"];
                })[0];

                if (check(expectedDraft, draft)) {
                    callback()
                } else {
                    callback(JSON.stringify(draft) + " did not match for " + JSON.stringify(expectedDraft))
                }
            })
        }, user);
    }

    public createUser(persona: Persona) {
        let that = this;
        return new Promise(function (resolve, reject) {
            that.authenticate(function (error, response, body) {
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

                request(options, function (error, response, body) {
                    if (response["statusCode"] == 201) {
                        resolve()
                    } else {
                        console.log("http createUser " + persona.properties['login'] + " " + response["statusCode"]);
                        reject("createUser failed")
                    }
                })
            });
        });//promise scope
    }

    public createOrganization(Organization: any) {
        let that = this;
        return new Promise(function (resolve, reject) {
            that.authenticate(function (error, response, body) {
                let options = {
                    method: 'POST',
                    url: browser.baseUrl + 'podiumuaa/api/organisations/',
                    headers: {
                        'Authorization': 'Bearer ' + parseJSON(body).access_token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(
                        {
                            "name": Organization.properties['name'],
                            "shortName": Organization.properties['shortName'],
                            "activated": true
                        }
                    )
                };
                request(options, function (error, response, body) {
                    if (response["statusCode"] == 201) {
                        resolve()
                    } else {
                        console.log("http createOrganization " + Organization.properties['shortName'] + " " + response["statusCode"]);
                        reject("createOrganization failed")
                    }
                })
            });
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
        let that = this;
        return new Promise(function (resolve, reject) {

            that.authenticate(function (error, response, body) {
                let options = {
                    method: 'GET',
                    url: browser.baseUrl + 'podiumuaa/api/test/clearDatabase',
                    headers: {
                        'Authorization': 'Bearer ' + parseJSON(body).access_token
                    },
                };
                request(options, function (error, response, body) {
                    if (response["statusCode"] == 200) {
                        resolve()
                    } else {
                        console.log("cleanDB returned: " + response["statusCode"]);
                        reject("cleanDB failed")
                    }
                })
            });
        });
    }

    assignRole(orgShortName: string, role: string, users: [string]) {
        let that = this;
        return new Promise(function (resolve, reject) {

            that.authenticate(function (error, response, body) {
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
                request(options, function (error, response, body) {
                    if (response["statusCode"] == 201) {
                        resolve();
                    } else {
                        console.log("http assignRole organisation", orgShortName, role, users, response["statusCode"]);
                        reject("assignRole failed");
                    }
                })
            });
        });

    }
}

function parseJSON(string: string) {
    if (string == '') {
        return string
    }
    return JSON.parse(string);
}
