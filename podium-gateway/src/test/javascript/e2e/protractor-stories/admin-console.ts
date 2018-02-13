/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import request = require('request-promise-native');
import initPersonaDictionary = require('../personas/persona-dictionary');
import { isNullOrUndefined, isUndefined } from 'util';
import { Persona } from '../personas/templates';
import { Organisation, Request } from '../data/templates';

let nonOrganisationAuthorities: string[] = ['ROLE_PODIUM_ADMIN', 'ROLE_BBMRI_ADMIN', 'ROLE_RESEARCHER'];

export class AdminConsole {

    apiUrl = 'http://localhost:8080/';

    constructor() {
    }

    public authenticate(persona?: Persona) {//store token first time
        let login;
        let password;

        if (!isUndefined(persona)) {
            login = persona["login"];
            password = persona["password"];
        } else {
            login = "admin";
            password = "admin";
        }

        let options = {
            method: 'POST',
            url: this.apiUrl + 'podiumuaa/oauth/token',
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

    public checkUser(persona: Persona, check) {
        return this.authenticate().then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/users/' + persona['login'],
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };

            return request(options).then((body) => {
                    let user = parseJSON(body);

                    if (!check(persona, user)) {
                        console.log("http checkUser " + persona['login']);
                        return "checkUser failed";
                    }
                }, (reason) => {
                    let user = parseJSON(reason['error']);

                    if (!check(persona, user)) {
                        console.log("http checkUser " + persona['login'] + " " + reason["statusCode"]);
                        return "checkUser failed";
                    }
                }
            )
        });
    }

    public getUsers() {
        return this.authenticate().then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/users/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    public getUser(persona: Persona) {
        return this.authenticate().then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/users/' + persona['login'],
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    public unlockUser(persona: Persona) {
        let token;

        this.authenticate().then((body) => {
            token = parseJSON(body).access_token;
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/users/' + persona['login'],
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            };
            return request(options).then((body) => {

                let user = parseJSON(body);

                let options = {
                    method: 'PUT',
                    url: this.apiUrl + 'podiumuaa/api/users/uuid/' + user.uuid + '/unlock',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                };
                return request(options).then((body), (reason) => {
                    console.log("http unlockUser " + persona['login'] + " " + reason["statusCode"], body);
                    return "unlockUser failed"
                })
            })
        })
    }

    public checkOrganisation(expectedOrganisation: Organisation, check) {

        return this.authenticate(initPersonaDictionary()['BBMRI_Admin']).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/organisations/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                let organisations = parseJSON(body);
                let organisation = organisations.filter(function (value) {
                    return value["shortName"] == expectedOrganisation["shortName"];
                })[0];

                if (!check(expectedOrganisation, organisation)) {
                    return JSON.stringify(organisation) + " did not match for " + JSON.stringify(expectedOrganisation)
                }

            })
        });
    }

    public getOrganisations() {
        return this.authenticate(initPersonaDictionary()['BBMRI_Admin']).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/organisations/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    public getOrganisation(organisation: Organisation) {
        return this.getOrganisations().then((organisations) => {
            return organisations.filter(function (value) {
                return value["shortName"] == organisation["shortName"];
            })[0];
        })
    }

    public getDrafts(persona: Persona) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'api/requests/drafts',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    public getDraft(persona: Persona, draft: Request) {
        return this.getDrafts(persona).then((drafts) => {
            return drafts.filter(function (value) {
                return value["requestDetail"]["title"] == draft["title"];
            })[0];
        });
    }

    public getFiles(persona: Persona, draft: Request) {

        return this.authenticate(persona).then((body) => {
            let token = parseJSON(body).access_token;

            let options = {
                method: 'GET',
                url: this.apiUrl + 'api/requests/drafts',
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            };
            return request(options).then((body) => {
                let draftUUID = parseJSON(body).filter(function (value) {
                    return value["requestDetail"]["title"] == draft["title"];
                })[0]['uuid'];

                let options = {
                    method: 'GET',
                    url: this.apiUrl + 'api/requests/' + draftUUID + '/files',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                };
                return request(options).then((body) => {
                    return parseJSON(body);
                })
            })
        })
    }

    public getRequests(persona: Persona, status, role: string) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'api/requests/status/' + status + '/' + role,
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    /*
     *  status ['Review', 'Delivery']
     *  role ['requester', ]
     */
    public getRequest(persona: Persona, status, role: string, filter: (body) => boolean) {
        return this.getRequests(persona, status, role).then((drafts) => {
            return drafts.filter((value) => {
                return filter(value);
            })[0];
        });
    }

    public validateRequest(persona: Persona, draft: Request) {
        return this.requestGetAction(persona, draft, 'validate');
    }

    public approveRequest(persona: Persona, draft: Request) {
        return this.requestGetAction(persona, draft, 'approve');
    }

    public startDelivery(persona: Persona, draft: Request) {
        return this.requestGetAction(persona, draft, 'startDelivery');
    }

    public getDeliveries(persona: Persona, draft: Request) {
        return this.requestGetAction(persona, draft, 'deliveries');
    }

    private requestGetAction(persona: Persona, draft: Request, action: string) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'api/requests/' + draft['uuid'] + '/' + action,
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    public rejectRequest(persona: Persona, draft: Request, note: { 'description': string, 'summary': string }) {
        return this.requestPostAction(persona, draft, 'reject', note);
    }


    public requestRevision(persona: Persona, draft: Request, note: { 'description': string, 'summary': string }) {
        return this.requestPostAction(persona, draft, 'requestRevision', note);
    }

    private requestPostAction(persona: Persona, draft: Request, action: string, note: { 'description': string, 'summary': string }) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'POST',
                url: this.apiUrl + 'api/requests/' + draft['uuid'] + '/' + action,
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'content-type': 'application/json'
                },
                body: JSON.stringify(note)
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    public releaseDeliveries(persona: Persona, delivery: Request, note: { 'description': string, 'summary': string }) {
        return this.requestPostAction(persona, delivery, 'release', note);
    }

    public receiveDeliveries(persona: Persona, delivery: Request, note: { 'description': string, 'summary': string }) {
        return this.requestPostAction(persona, delivery, 'received', note);
    }

    private deliveriesPostAction(persona: Persona, delivery: {}, action: string, note: { 'description': string, 'summary': string }) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'POST',
                url: this.apiUrl + 'api/requests/' + delivery['historicEvents'][0]['data']['requestUuid'] + '/deliveries/' + delivery['uuid'] + '/' + action,
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'content-type': 'application/json'
                },
                body: JSON.stringify(note)
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    public createUser(persona: Persona) {
        return this.authenticate().then((body) => {
            let userData = {
                "id": null,
                "login": persona['login'],
                "firstName": persona['firstName'],
                "lastName": persona['lastName'],
                "email": persona['email'],
                "telephone": persona['telephone'],
                "institute": persona['institute'],
                "department": persona['department'],
                "jobTitle": persona['jobTitle'],
                "specialism": persona['specialism'],
                "emailVerified": persona['emailVerified'],
                "adminVerified": persona['adminVerified'],
                "accountLocked": persona['accountLocked'],
                "langKey": "en",
                "createdBy": null,
                "createdDate": null,
                "lastModifiedBy": null,
                "lastModifiedDate": null,
                "password": persona['password']
            };
            // Set non-organisation authorities
            let roles: any[] = persona['authority'];
            if (!isNullOrUndefined(roles)) {
                userData['authorities'] = roles
                    .map((role) => role['role'])
                    .filter((authority) => nonOrganisationAuthorities.indexOf(authority) >= 0);
            }
            let options = {
                method: 'POST',
                url: this.apiUrl + 'podiumuaa/api/test/users',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            };

            return request(options).catch((reason) => {
                console.log("http createUser " + persona['login'], reason["statusCode"]);
                return "createUser failed"
            });
        });
    }

    public createOrganisation(persona: Persona, organisation: Organisation) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'POST',
                url: this.apiUrl + 'podiumuaa/api/organisations/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(
                    {
                        "name": organisation['name'],
                        "shortName": organisation['shortName'],
                        "activated": organisation['activated'],
                        "requestTypes": organisation['requestTypes']
                    }
                )
            };
            return request(options).catch((reason) => {
                    console.log("http createorganisation " + organisation['shortName'] + " " + reason["statusCode"]);
                    return "createorganisation failed"
                }
            )
        });
    }

    public createRequest(persona: Persona, request: Request) {
        return this.newDraft(persona).then((draft) => {
            return this.constructRequest(persona, draft, request).then((draft) => {
                return this.saveDraft(persona, draft).then((draft) => {
                    return this.submitDraft(persona, draft);
                })
            })
        })
    }

    public cleanDB() {
        return this.authenticate().then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/test/clearDatabase',
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

    public assignRole(orgShortName: string, role: string, users: [string]) {
        return this.authenticate().then((body) => {
            let options = {
                method: 'POST',
                url: this.apiUrl + 'podiumuaa/api/test/roles/assign',
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

    public getOrgUUID(orgShortName: string) {
        return this.authenticate(initPersonaDictionary()['BBMRI_Admin']).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/organisations/',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token
                }
            };

            return request(options).then((body) => {
                let organizations = parseJSON(body);
                let organization = organizations.filter(function (value) {
                    return value["shortName"] == orgShortName;
                })[0];
                return organization["uuid"] as string;
            }).catch((reason) => {
                console.log("http getOrgUUID", orgShortName, reason["statusCode"], body);
                return "getOrgUUID failed";
            })
        });
    }

    public createDraft(persona: Persona, request: Request) {
        return this.newDraft(persona).then((draft) => {
            return this.constructRequest(persona, draft, request).then((draft) => {
                return this.saveDraft(persona, draft)
            })
        })
    }

    public newDraft(persona: Persona) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'POST',
                url: this.apiUrl + 'api/requests/drafts',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            });
        })
    }

    public saveDraft(persona: Persona, draft) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'PUT',
                url: this.apiUrl + 'api/requests/drafts',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(draft)
            };
            return request(options).then((body) => {
                return parseJSON(body);
            });
        })
    }

    public submitDraft(persona: Persona, draft) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'api/requests/drafts/' + draft['uuid'] + '/submit',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            });
        })
    }

    public getAccount(persona: Persona) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/account',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            });
        })
    }

    public getAvailableOrganisations(persona: Persona) {
        return this.authenticate(persona).then((body) => {
            let options = {
                method: 'GET',
                url: this.apiUrl + 'podiumuaa/api/organisations/available',
                headers: {
                    'Authorization': 'Bearer ' + parseJSON(body).access_token,
                    'Content-Type': 'application/json'
                }
            };
            return request(options).then((body) => {
                return parseJSON(body);
            })
        })
    }

    public constructRequest(persona: Persona, draft, request: Request) {
        return this.getAccount(persona).then((requester) => {
            draft['requester'] = requester;
            return this.getAvailableOrganisations(persona).then((availableOrganisations: {}[]) => {
                return availableOrganisations.filter((org) => {
                    return request['organisations'].indexOf(org['shortName']) > -1;
                })
            }).then((result) => {
                draft['organisations'] = result;
                setRequestDetails(draft, request);
                return draft
            });
        })
    }
}

function setRequestDetails(draft, request: Request) {
    let requestDetails = draft['requestDetail'];
    let principalInvestigator = requestDetails['principalInvestigator'];

    //request
    ['title', 'background', 'researchQuestion', 'hypothesis', 'methods', 'relatedRequestNumber',
        'searchQuery', 'requestType', 'combinedRequest'].forEach((fieldName) => {
        requestDetails[fieldName] = request[fieldName];
    });

    //principal Investigator
    ['name', 'email', 'jobTitle', 'affiliation'].forEach((fieldName) => {
        principalInvestigator[fieldName] = request[fieldName];
    });
}

function parseJSON(string: string) {
    if (string == '') {
        return string
    }
    return JSON.parse(string);
}
