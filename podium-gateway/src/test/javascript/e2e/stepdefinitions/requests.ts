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
import { checkTextElement, copyData, countIs, doInOrder, login, promiseTrue, roleToRoute } from './util';
import { Organisation, Request } from '../data/templates';
import { $, $$, browser, protractor } from 'protractor';
import { RequestOverviewStatusOption } from '../../../../main/webapp/app/shared/request/request-status/request-status.constants';
import { isUndefined } from 'util';

let { defineSupportCode } = require('cucumber');


defineSupportCode(({ Given, When, Then }) => {

    When(/^(.*) fills the new draft with data from '(.*)'$/, function (personaName, requestName): Promise<any> {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let request: Request = director.getData(requestName);
        this.scenarioData = copyData(request); //store for next step

        return doInOrder(['title', 'background', 'researchQuestion', 'hypothesis', 'methods', 'relatedRequestNumber',
            'searchQuery', 'name', 'email', 'jobTitle', 'affiliation'], (key) => {
            return director.enterText(key, request[key]);
        }).then(() => {
            return doInOrder(request["requestType"], (type) => {
                return director.clickOn(type);
            }).then(() => {
                return doInOrder(request['organisations'], (organisation) => {
                    return browser.actions().mouseMove($('.test-option-' + organisation)).keyDown(platformKey())
                        .click().keyUp(platformKey()).perform().then(() => {
                            if (request['combinedRequest']) {
                                return director.clickOn('combinedRequestYes');
                            }
                        });
                }).then(() => {
                    return director.clickOn("save")
                });
            })
        })
    });

    function platformKey(): string {
        switch (process.platform) {
            case 'darwin': {
                return protractor.Key.COMMAND;
            }
            default: {
                return protractor.Key.CONTROL;
            }
        }
    }

    When(/^(.*) submits the draft$/, function (personaName) {
        let director = this.director as Director;

        return director.clickOn('submit').then(() => {
            return director.clickOn('submit-modal')
        })
    });

    Then('the draft is removed', function () {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');

        return browser.sleep(500).then(() => {
            return adminConsole.getDraft(persona, this.scenarioData).then((body) => {
                return promiseTrue(isUndefined(body), `the draft was not removed, getDrafts returned: ${body}`)
            });
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

        return browser.sleep(500).then(() => {
            return adminConsole.getDraft(persona, this.scenarioData).then((body) => {
                let revisedRequest = this.scenarioData;
                let revisionDetail = body['requestDetail'];

                return checkRequestDetails(revisionDetail, revisedRequest).then(() => {
                    return promiseTrue(JSON.stringify(body['organisations'].map((org) => {
                        return org['name']
                    }).sort(alphabetically)) == JSON.stringify(revisedRequest['organisations'].map((org) => {
                        return director.getData(org)['name']
                    }).sort(alphabetically)), 'organisations')
                });
            });
        });
    });

    Given(/^(.*) opens the draft '(.*)'$/, function (personaName, requestName) {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;
        let request = director.getData(requestName);
        let persona = director.getPersona(personaName);
        this.scenarioData = copyData(request); //store for next step

        return login(director, persona).then(() => {
            return director.goToPage('request overview').then(() => {
                return $('.test-request-row-' + requestName).$('.test-edit-btn').click().then(() => {
                    return director.at('edit requests');
                })
            })
        })
    });

    Given(/^(.*) goes to the '(.*)' page for the request '(.*)' submitted to '(.*)'$/, function (personaName, pageName, requestName, orgShortName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;
        let request = director.getData(requestName);
        let persona = director.getPersona(personaName);
        let organisation = director.getData(orgShortName);
        this.scenarioData = copyData(request); //store for next step

        return login(director, persona).then(() => {
            return adminConsole.getRequest(persona, RequestOverviewStatusOption.All, roleToRoute(persona, orgShortName), (body) => {
                return body["requestDetail"]["title"] == request["title"] &&
                    body['organisations'].every((org) => {
                        return org['name'] == organisation['name']
                    });
            }).then((sufix) => {
                return director.goToPage(pageName, sufix['uuid'] as string)
            })
        });
    });

    Then(/^the request details for '(.*)' submitted to '(.*)' are shown$/, function (requestName, orgShortName): Promise<any> {
        let director = this.director as Director;
        let page = director.getCurrentPage();
        let request = director.getData(requestName);
        let organisation = director.getData(orgShortName);

        return Promise.all([
            ...['title', 'background', 'researchQuestion', 'hypothesis', 'methods', 'relatedRequestNumber',
                'searchQuery', 'name', 'email', 'jobTitle', 'affiliation'].map((fieldName) => {
                return checkTextElement(director.getLocator(fieldName), request[fieldName])
            }),
            checkTextElement(director.getLocator('organisations'), organisation['name']),
            checkRequestTypes(organisation['requestTypes'], director.getLocator('requestType'))
                .then(() => {
                    if (request['combinedRequest']) {
                        return checkTextElement(director.getLocator('combinedRequest'), 'Yes')
                    } else {
                        return checkTextElement(director.getLocator('combinedRequest'), 'No')
                    }
                })
        ])
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
            return checkTable(fields, requests, director).then(() => {
            }, (error) => {
                if (requestNamesListAltOrder.length == 0) {
                    return Promise.reject(error);
                }
                let requests: Request[] = director.getListOfData(requestNamesListAltOrder);
                fields = fieldNames.split(", ");
                return checkTable(fields, requests, director);
            })
        })
    });

    function checkTable(fields: any[] | string[], requests: Request[], director: Director) {
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
                return checkRequestTypes(request['requestType'], element);
            }
            case 'organisations': {
                return checkOrganisations(request['organisations'], element, director);
            }
            case 'requesterName': {
                let requester = director.getPersona(request['requesterDataId']);
                return checkTextElement(element, `${requester['firstName']} ${requester['lastName']}`);
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
        let persona = director.getPersona(this.scenarioData['requesterDataId']);
        let request = this.scenarioData;
        let organisation = director.getData(request['organisations'][0]);

        return adminConsole.getRequest(persona, RequestOverviewStatusOption.All, roleToRoute(persona), (body) => {
            return body["requestDetail"]["title"] == request["title"] &&
                body['organisations'].every((org) => {
                    return org['name'] == organisation['name']
                });
        }).then((request) => {
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
        let persona = director.getPersona(this.scenarioData['requesterDataId']);
        let request = this.scenarioData;
        let organisation = director.getData(request['organisations'][0]);

        return adminConsole.getRequest(persona, RequestOverviewStatusOption.All, roleToRoute(persona), (body) => {
            return body["requestDetail"]["title"] == request["title"] &&
                body['organisations'].every((org) => {
                    return org['name'] == organisation['name']
                });
        }).then((request) => {
            return adminConsole.validateRequest(director.getPersona('Request_Coordinator'), request);
        })
    });

    Given(/^'(.*)' is approved$/, function (requestName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;
        this.scenarioData = copyData(director.getData(requestName)); //store for next step
        let persona = director.getPersona(this.scenarioData['requesterDataId']);
        let request = this.scenarioData;
        let organisation = director.getData(request['organisations'][0]);

        return adminConsole.getRequest(persona, RequestOverviewStatusOption.All, roleToRoute(persona), (body) => {
            return body["requestDetail"]["title"] == request["title"] &&
                body['organisations'].every((org) => {
                    return org['name'] == organisation['name']
                });
        }).then((request) => {
            return adminConsole.validateRequest(director.getPersona('Request_Coordinator'), request).then((request) => {
                return adminConsole.approveRequest(director.getPersona('Request_Coordinator'), request)
            })
        })
    });

    Given(/^'(.*)'s delivery has started/, function (requestName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;
        this.scenarioData = copyData(director.getData(requestName)); //store for next step
        let persona = director.getPersona(this.scenarioData['requesterDataId']);
        let request = this.scenarioData;
        let organisation = director.getData(request['organisations'][0]);

        return adminConsole.getRequest(persona, RequestOverviewStatusOption.All, roleToRoute(persona), (body) => {
            return body["requestDetail"]["title"] == request["title"] &&
                body['organisations'].every((org) => {
                    return org['name'] == organisation['name']
                });
        }).then((request) => {
            return adminConsole.validateRequest(director.getPersona('Request_Coordinator'), request).then((request) => {
                return adminConsole.approveRequest(director.getPersona('Request_Coordinator'), request).then((request) => {
                    return adminConsole.startDelivery(director.getPersona('Request_Coordinator'), request);
                })
            })
        })
    });

    When(/^(.*) revises and '(.*)s' the request$/, function (personaName, action): Promise<any> {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let request: Request = this.scenarioData;

        return doInOrder(['title', 'background', 'researchQuestion', 'hypothesis', 'methods', 'relatedRequestNumber',
            'searchQuery', 'name', 'email', 'jobTitle', 'affiliation'], (key) => {
            request[key] = request[key] + 'revision';
            return director.enterText(key, request[key]);
        }).then(() => {
            return director.clickOn(action)
        })
    });

    Then(/^the request has the status '(.*)'$/, function (requestState): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');
        let request = this.scenarioData;
        let orgShortName = this.scenarioData['organisations'][0];
        let organisation = director.getData(orgShortName);

        return browser.sleep(500).then(() => {
            return adminConsole.getRequest(persona, RequestOverviewStatusOption.All, roleToRoute(persona, orgShortName), (body) => {
                return body["requestDetail"]["title"] == request["title"] &&
                    body['organisations'].every((org) => {
                        return org['name'] == organisation['name']
                    });
            }).then((body) => {
                return promiseTrue(body['status'] == requestState, 'request ' + body['requestDetail']['title'] + ' is not in ' + requestState + '\n ' + JSON.stringify(body))
            })
        });
    });

    Then(/^there are the following deliveries:$/, function (expectedDeliveriesString): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let expectedDeliveries: { type: string, status: string }[] = JSON.parse(expectedDeliveriesString);
        let persona = director.getPersona('he');
        let request = this.scenarioData;
        let orgShortName = request['organisations'][0];
        let organisation = director.getData(orgShortName);

        return browser.sleep(500).then(() => {
            return adminConsole.getRequest(persona, RequestOverviewStatusOption.Delivery, roleToRoute(persona, orgShortName), (body) => {
                return body["requestDetail"]["title"] == request["title"] &&
                    body['organisations'].every((org) => {
                        return org['name'] == organisation['name']
                    });
            }).then((body) => {
                return adminConsole.getDeliveries(persona, body).then((deliveries) => {
                    return Promise.all([
                        promiseTrue(expectedDeliveries.length == deliveries.length, 'expected ' + JSON.stringify(expectedDeliveries) + '\nbut found: ' + JSON.stringify(deliveries)),
                        ...expectedDeliveries.map((expected) => {
                            return promiseTrue(deliveries.some((delivery) => {
                                return (expected['type'] == delivery['type'] && expected['status'] == delivery['status'])
                            }), 'Could not find a matching delivery for: ' + JSON.stringify(expected) + '\nin: ' + JSON.stringify(deliveries))
                        })
                    ])
                })
            })
        });
    });

    Then(/^the revision for '(.*)' is saved$/, function (requestName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');
        let request = director.getData(requestName);
        let orgShortName = request['organisations'][0];
        let organisation = director.getData(orgShortName);

        return adminConsole.getRequest(persona, RequestOverviewStatusOption.Revision, roleToRoute(persona, orgShortName), (body) => {
            return body["requestDetail"]["title"] == request["title"] &&
                body['organisations'].every((org) => {
                    return org['name'] == organisation['name']
                });
        }).then((body) => {
            let revisedRequest = this.scenarioData;
            let revisionDetail = body['revisionDetail'];

            return checkRequestDetails(revisionDetail, revisedRequest).then(() => {
                return promiseTrue(body['organisations'][0]['name'] == director.getData(revisedRequest['organisations'][0])['name'], 'organisations')
            });
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

    When(/^(.*) releases delivery '(.*)'$/, function (personaName, deliveryTypesString) {
        let director = this.director as Director;
        let deliveryTypes = deliveryTypesString.split(", ");

        return doInOrder(deliveryTypes, (deliveryType) => {
            return browser.waitForAngular().then(() => { //wait for the popover to disappear from the previous step
                return director.clickOnElement(director.getElement('deliveryrow' + deliveryType).locator.$('.test-delivery-action-btn')).then((): Promise<any> => {
                    return director.enterText('reference', 'release Note ' + deliveryType, protractor.Key.ENTER)
                })
            })
        })
    });

    When(/^(.*) marks released delivery '(.*)' as received$/, function (personaName, deliveryTypesString) {
        let director = this.director as Director;
        let deliveryTypes = deliveryTypesString.split(", ");

        return doInOrder(deliveryTypes, (deliveryType) => {
            return browser.waitForAngular().then(() => { //wait for the popover to disappear from the previous step
                return director.clickOnElement(director.getElement('deliveryrow' + deliveryType).locator.$('.test-delivery-action-btn'))
            })
        });
    });

    When(/^(.*) finalises the request$/, function (personaName) {
        let director = this.director as Director;

        return browser.waitForAngular().then(() => {
            return director.clickOn('finalize').then(() => {
                return director.clickOn('finalizeSubmit')
            })
        })
    });

    When(/^(.*) cancels delivery '(.*)'$/, function (personaName, deliveryTypesString) {
        let director = this.director as Director;
        let deliveryTypes = deliveryTypesString.split(", ");

        return doInOrder(deliveryTypes, (deliveryType) => {
            return browser.waitForAngular().then(() => { //wait for the popover to disappear from the previous step
                return director.clickOnElement($('.test-dropdown-toggle-' + deliveryType)).then(() => {
                    return director.clickOnElement($('.test-dropdown-menu-' + deliveryType).$('.dropdown-item')).then((): Promise<any> => {
                        return Promise.all([
                            director.enterText('messageSummary', 'cancels delivery messageSummary'),
                            director.enterText('messageDescription', 'cancels delivery messageDescription')
                        ]).then(() => {
                            return director.clickOn('submit')
                        })
                    })
                });
            })
        });
    });
});

function checkRequestDetails(details, request) {
    let pi = details['principalInvestigator'];

    return Promise.all([
        ...['title', 'background', 'researchQuestion', 'hypothesis', 'methods', 'relatedRequestNumber',
            'searchQuery', 'combinedRequest'].map((field) => {
            return promiseTrue(details[field] == request[field], `data for field ${field} did not match details: \n ${JSON.stringify(details)} \n expected: \n ${JSON.stringify(request)}`)
        }),
        ...['name', 'email', 'jobTitle', 'affiliation'].map((field) => {
            return promiseTrue(pi[field] == request[field], `data for field ${field} did not match pi: \n ${JSON.stringify(pi)} \n expected: \n ${JSON.stringify(request)}`)
        }),
        promiseTrue(JSON.stringify(details['requestType'].sort(alphabetically)) == JSON.stringify(request['requestType'].sort(alphabetically)), `${JSON.stringify(details['requestType'].sort(alphabetically))} ${JSON.stringify(request['requestType'].sort(alphabetically))}`)
    ])
}

function alphabetically(a, b) {
    if (a < b) return -1;
    if (a > b) return 1;
    return 0;
}
