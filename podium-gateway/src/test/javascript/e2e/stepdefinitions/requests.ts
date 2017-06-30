/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { Director } from '../protractor-stories/director';
import { AdminConsole } from '../protractor-stories/admin-console';
import { Promise } from 'es6-promise';
import { doInOrder, promiseTrue, login, checkTextElement, roleToRoute, copyData, countIs } from './util';
import { Organisation, Request } from '../data/templates';
import { $$, browser } from 'protractor';
let { defineSupportCode } = require('cucumber');


defineSupportCode(({ Given, When, Then }) => {

    When(/^(.*) creates a new draft filling data for '(.*)'$/, function (personaName, requestName): Promise<any> {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let request: Request = director.getData(requestName);
        this.scenarioData = request; //store for next step
        let page = director.getCurrentPage();

        return director.clickOn("new draft").then(() => {
            return doInOrder(["title", "background", "research question", "hypothesis", "methods", "related request number", "piName",
                "piEmail", "piFunction", "piAffiliation", "searchQuery"], (key) => {
                return director.enterText(key, request[key]);
            }).then(() => {
                return doInOrder(request["requestTypes"], (type) => {
                    return director.clickOn(type);
                }).then(() => {
                    return director.clickOn("save")
                })
            })
        });

    });

    When(/^(.*) selects request types '(.*)'$/, function (personaName, requestTypes): Promise<any> {
        let director = this.director as Director;
        let types = requestTypes.split(", ");

        return doInOrder(types, (type) => {
            return director.clickOn(type);
        })
    });

    Then(/^the organisations '(.*)' can be selected$/, function (organisationNames): Promise<any> {
        let director = this.director as Director;
        let orgList = organisationNames.split(", ");
        let orgNames: string[] = [];

        director.getListOfData(orgList).forEach((org: Organisation) => {
            orgNames.push(org["name"]);
        });

        return countIs(director.getElement("organisations").locator.$$('option'), orgNames.length).then(() => {
            return director.getElement("organisations").locator.$$('option').each((element) => {
                return element.getText().then((text) => {
                    return promiseTrue(orgNames.some((item) => {
                        return text.trim() == item;
                    }), "\"" + text + "\"" + "Should not be selectable only: " + orgNames)
                })
            })
        })
    });

    Then(/^the draft is saved$/, function (): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');

        return adminConsole.getDraft(persona, this.scenarioData).then((draft) => {
            return promiseTrue(draft['title'] == this.scenarioData['title'], this.scenarioData + ' did not match ' + draft);
        })
    });

    Given(/^(.*) goes to the '(.*)' page for the request '(.*)' submitted to '(.*)'$/, function (personaName, pageName, requestName, orgShortName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;
        let request = director.getData(requestName)
        let persona = director.getPersona(personaName);
        let organisation = director.getData(orgShortName);
        this.scenarioData = copyData(request); //store for next step

        return login(director, persona).then(() => {
            return adminConsole.getRequest(persona, 'All', roleToRoute(persona, orgShortName), request, organisation['name']).then((sufix) => {
                return director.goToPage(pageName, sufix['uuid'] as string)
            })
        });
    });

    Then(/^the request details for '(.*)' submitted to '(.*)' are shown$/, function (requestName, orgShortName): Promise<any> {
        let director = this.director as Director;
        let page = director.getCurrentPage();
        let request = director.getData(requestName);
        let organisation = director.getData(orgShortName);

        let promisses = [
            checkTextElement(page.elements['title'].locator, request['title']),
            checkTextElement(page.elements['searchQuery'].locator, request['searchQuery']),
            checkTextElement(page.elements['background'].locator, request['background']),
            checkTextElement(page.elements['researchQuestion'].locator, request['research question']),
            checkTextElement(page.elements['hypothesis'].locator, request['hypothesis']),
            checkTextElement(page.elements['methods'].locator, request['methods']),
            checkTextElement(page.elements['piName'].locator, request['piName']),
            checkTextElement(page.elements['piEmail'].locator, request['piEmail']),
            checkTextElement(page.elements['piFunction'].locator, request['piFunction']),
            checkTextElement(page.elements['piAffiliation'].locator, request['piAffiliation']),
            checkTextElement(page.elements['organisations'].locator, organisation['name']),
            checkRequestTypes(organisation['requestTypes'], $$('.test-requestTypes'))
        ];

        return Promise.all(promisses);
    });

    function checkRequestTypes(requestTypes: string[], elements) {
        return countIs(elements, requestTypes.length).then(() => {
            return elements.each((element) => {
                return element.$('.test-requestType-text').getText().then(function (type) {
                    return promiseTrue(requestTypes.indexOf(type) > -1, type + " is not part of " + requestTypes);
                })
            })
        })
    }

    function checkOrganisations(organisations: string[], elements, director: Director) {
        organisations = director.getListOfData(organisations);
        let orgNames = organisations.map((org) => {
            return org['name']
        });

        return countIs(elements, organisations.length).then(() => {
            return elements.each((element) => {
                return element.getText().then(function (orgName) {
                    return promiseTrue(orgNames.indexOf(orgName) > -1, orgName + " is not part of " + orgNames);
                })
            })
        })
    }

    Then(/^the overview contains the request's '(.*)' for the requests '(.*)'(.*)$/, function (fieldNames, requestNames, altOrder): Promise<any> {
        let director = this.director as Director;

        let fields = fieldNames.split(", ");
        let requestNamesList = requestNames.trim().split(", ");
        let requestNamesListAltOrder = [];
        if (altOrder.trim() != '') {
            requestNamesListAltOrder = altOrder.trim().split(", ");
        }

        let requests: Request[] = director.getListOfData(requestNamesList);

        return countIs($$('.test-' + fields[0]), requests.length).then(() => {
            return checkTable(fields, requests, director);
        }).then(() => {
        }, (error) => {
            if (requestNamesListAltOrder.length == 0) {
                return Promise.reject(error);
            }

            let requests: Request[] = director.getListOfData(requestNamesListAltOrder);
            fields = fieldNames.split(", ");
            return checkTable(fields, requests, director);
        })
    });

    function checkTable(fields: any[]|string[], requests: Request[], director: Director) {
        return doInOrder(fields, (field) => {
            return $$('.test-' + field).isPresent().then((present) => {
                return promiseTrue(present, 'field ' + '.test-' + field + ' could not be found')
            }).then(() => {
                return $$('.test-' + field).each((element, index) => {
                    return checkField(element, field, requests[index], director);
                });
            });
        })
    }

    function checkField(element, field, request: Request, director: Director): Promise<any> {
        if (['requestTypes', 'organisations'].indexOf(field) > -1) {
            element = element.$$('li');
        }

        switch (field) {
            case 'requestTypes': {
                return checkRequestTypes(request['requestTypes'], element);
            }
            case 'organisations': {
                return checkOrganisations(request['organisations'], element, director);
            }
            default: {
                return checkTextElement(element, request[field]);
            }
        }
    }

    Given(/^'(.*)' needs revision$/, function (requestName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;
        this.scenarioData = copyData(director.getData(requestName)); //store for next step

        return adminConsole.getRequest(director.getPersona('Linda'), 'All', 'requester', director.getData('Request02'), director.getData(director.getData(requestName)['organisations'][0])['name']).then((request) => {
            return adminConsole.requestRevision(director.getPersona('Request_Coordinator'), request, {
                "summary": "sum",
                "description": "des"
            });
        })
    });

    Given(/^'(.*)' needs review$/, function (requestName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;
        this.scenarioData = copyData(director.getData(requestName)); //store for next step

        return adminConsole.getRequest(director.getPersona('Linda'), 'All', 'requester', director.getData('Request02'), director.getData(director.getData(requestName)['organisations'][0])['name']).then((request) => {
            return adminConsole.validateRequest(director.getPersona('Request_Coordinator'), request);
        })
    });

    When(/^(.*) revises and '(.*)s' the request$/, function (personaName, action): Promise<any> {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let request: Request = this.scenarioData;

        return doInOrder(["title", "background", "research question", "hypothesis", "methods", "related request number", "piName",
            "piEmail", "piFunction", "piAffiliation", "searchQuery"], (key) => {
            request[key] = request[key] + 'revision';
            return director.enterText(key, request[key]);
        }).then(() => {
            return director.clickOn(action)
        })
    });

    Then(/^the request is in Review with status '(.*)'$/, function (requestState): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');
        let orgShortName = this.scenarioData['organisations'][0];

        return browser.sleep(1000).then(() => {
            return adminConsole.getRequest(persona, 'All', roleToRoute(persona, orgShortName), this.scenarioData, director.getData(orgShortName)['name']).then((body) => {
                return promiseTrue((body['status'] == 'Review') && (body['requestReview']['status'] == requestState), 'request ' + body['requestDetail']['title'] + ' is not in ' + requestState)
            })
        });
    });

    Then(/^the request has the status '(.*)'$/, function (requestState): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');
        let orgShortName = this.scenarioData['organisations'][0];

        return browser.sleep(1000).then(() => {
            return adminConsole.getRequest(persona, requestState, roleToRoute(persona, orgShortName), this.scenarioData, director.getData(orgShortName)['name']).then((body) => {
                return promiseTrue(body['status'] == requestState, 'request ' + body['requestDetail']['title'] + ' is not in ' + requestState)
            })
        });
    });

    Then(/^the request is closed with outcome '(.*)'$/, function (outCome): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');
        let orgShortName = this.scenarioData['organisations'][0];

        return browser.sleep(1000).then(() => {
            return adminConsole.getRequest(persona, 'All', roleToRoute(persona, orgShortName), this.scenarioData, director.getData(orgShortName)['name']).then((body) => {
                return promiseTrue((body['status'] == 'Closed') && body['outcome'] == outCome, 'request ' + body['requestDetail']['title'] + ' is not ' + 'Closed')
            })
        });
    });

    Then(/^the revision for '(.*)' is saved$/, function (requestName): Promise<any> {

        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');
        let request = director.getData(requestName);

        return adminConsole.getRequest(persona, 'Revision', 'requester', request, director.getData(this.scenarioData['organisations'][0])['name']).then((body) => {
            let revisedRequest = this.scenarioData;
            let revisionDetail = body['revisionDetail'];

            return Promise.all([
                promiseTrue(revisionDetail['title'] == revisedRequest['title'], 'title'),
                promiseTrue(revisionDetail['searchQuery'] == revisedRequest['searchQuery'], 'searchQuery'),
                promiseTrue(revisionDetail['background'] == revisedRequest['background'], 'background'),
                promiseTrue(revisionDetail['researchQuestion'] == revisedRequest['research question'], 'research question'),
                promiseTrue(revisionDetail['hypothesis'] == revisedRequest['hypothesis'], 'hypothesis'),
                promiseTrue(revisionDetail['methods'] == revisedRequest['methods'], 'methods'),
                promiseTrue(revisionDetail['principalInvestigator']['name'] == revisedRequest['piName'], 'piName ' + revisionDetail['piName'] + ' ' + revisedRequest['piName']),
                promiseTrue(revisionDetail['principalInvestigator']['email'] == revisedRequest['piEmail'], 'piEmail'),
                promiseTrue(revisionDetail['principalInvestigator']['jobTitle'] == revisedRequest['piFunction'], 'piFunction'),
                promiseTrue(revisionDetail['principalInvestigator']['affiliation'] == revisedRequest['piAffiliation'], 'piAffiliation'),
                promiseTrue(body['organisations'][0]['name'] == director.getData(revisedRequest['organisations'][0])['name'], 'organisations'),
                promiseTrue(JSON.stringify(revisionDetail['requestType'].sort(alphabetically)) == JSON.stringify(revisedRequest['requestTypes'].sort(alphabetically)), 'requestTypes')
            ])
        })

    });

    When(/^(.*) sends the request for review$/, function (personaName): Promise<any> {
        let director = this.director as Director;

        return director.clickOn('validationCheck').then(() => {
            return director.clickOn('validate')
        })
    });

    When(/^(.*) approves the request$/, function (personaName): Promise<any> {
        let director = this.director as Director;

        return director.clickOn('approve')
    });

    When(/^(.*) closes the request$/, function (personaName): Promise<any> {
        let director = this.director as Director;

        return director.clickOn('close')
    });

    When(/^(.*) starts delivery on the request$/, function (personaName): Promise<any> {
        let director = this.director as Director;

        return director.clickOn('startDelivery')
    });

    When(/^(.*) rejects the request$/, function (personaName): Promise<any> {
        let director = this.director as Director;

        return director.clickOn('reject').then(() => {
            return Promise.all([
                director.enterText('messageSummary', 'rejected messageSummary'),
                director.enterText('messageDescription', 'rejected messageDescription')
            ]).then(() => {
                return director.clickOn('submit')
            })
        })
    });

    When(/^(.*) sends the request back for revision$/, function (personaName): Promise<any> {
        let director = this.director as Director;

        return director.clickOn('revision').then(() => {
            return Promise.all([
                director.enterText('messageSummary', 'rejected messageSummary'),
                director.enterText('messageDescription', 'rejected messageDescription')
            ]).then(() => {
                return director.clickOn('submit')
            })
        })
    });

    Then('the request cannot be edited', function (): Promise<any> {
        return Promise.all([
            countIs($$('textarea'), 0),
            countIs($$('input'), 1) //only a checkbox for validationCheck
        ])
    });
});

function alphabetically(a, b) {
    if (a < b) return -1;
    if (a > b) return 1;
    return 0;
}
