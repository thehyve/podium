/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {browser, ElementFinder} from "protractor";
import {Promise} from "es6-promise";
import {isUndefined} from "util";

export interface Persona {
    name: string;
    properties: {[key: string]: any};
}

export interface Page {
    name: string;
    url: string;
    at?(): Promise<boolean>;
    ignoreSynchronization?: boolean;
    elements: {[name: string]: Interactable};
}

export interface Interactable {
    locator: ElementFinder;
    destination?: string; //name of the expected page after the element is clicked
    strict?: boolean; //if true will call the at() function after the transition
}

/*
 *
 * The main class threw which actions should be taken. Using the 'Persona' and 'Page' interfaces, it provides
 * additional page/persona tracking.
 */
export class Director {
    private searchDir: string;
    private currentPage: Page;
    private currentPersona: Persona;
    private pageDictionary: {[key: string]: Page};
    private personaDictionary: {[key: string]: Persona};

    constructor(searchDir: string, PageDictionary: {[key: string]: Page}, personaDictionary: {[key: string]: Persona}) {
        this.searchDir = searchDir;
        this.pageDictionary = PageDictionary;
        this.personaDictionary = personaDictionary;
        browser.get('/');
        browser.executeScript('localStorage.clear();');
        browser.executeScript('sessionStorage.clear();');
    }

    fatalError(message: string) {
        throw 'Fatal error: the specification is incorrectly expressed! ' + message;
    }

    private setCurrentPageTo(pageName: string) {
        if (!(pageName in this.pageDictionary)) {
            this.fatalError('The page: ' + pageName + ' does not exist.\n check your pageDictionary to see the available pages');
        }
        this.currentPage = this.pageDictionary[pageName];
        browser.ignoreSynchronization = isUndefined(this.currentPage.ignoreSynchronization) ? false : this.currentPage.ignoreSynchronization;
        return this.currentPage
    }

    public getCurrentPage() {
        return this.currentPage;
    }

    private setCurrentPersonaTo(personaName: string) {
        if (!(personaName in this.personaDictionary)) {
            this.fatalError('The persona: ' + personaName + ' does not exist.\n check your personaDictionary to see the available personas');
        }
        return this.currentPersona = this.personaDictionary[personaName];
    }

    public getPersona(personaName: string) {
        if (personaName != "he" && personaName != "she") {
            this.setCurrentPersonaTo(personaName);
        }
        return this.currentPersona;
    }

    public getListOfPersonas(personaNames: string[]) {
        let that = this;
        let result = new Array(personaNames.length);

        personaNames.forEach(function (personaName, index) {
            if (isUndefined(that.personaDictionary[personaName])){
                that.fatalError('The persona: ' + personaName + ' does not exist.\n check your personaDictionary to see the available personas');
            }
            result[index] = that.personaDictionary[personaName];
        });

        return result;
    }

    public goToPage(pageName: string, sufix?: string) {
        let page = this.setCurrentPageTo(pageName);
        let url = isUndefined(sufix) ? page.url : page.url + sufix;

        return browser.get(url);
    }

    public at(pageName: string) {
        let page = this.setCurrentPageTo(pageName);
        // browser.waitForAngular('make sure the page is loaded before doing a check');
        return Promise.resolve(page.at()).then(function (v) {
            return new Promise(function (resolve, reject) {
                if (v) {
                    resolve();
                }
                else {
                    reject(Error('not at page: ' + pageName));
                }
            })
        });
    };

    public getElement(elementName: string) {
        let element = this.getCurrentPage().elements[elementName];
        if (isUndefined(element)) {
            this.fatalError('The page: ' + this.getCurrentPage().name + ' does not have an element for ' + elementName + '.\n');
        }
        return element;
    }

    private handleDestination(element: Interactable) {
        if (!isUndefined(element.destination)) {
            this.setCurrentPageTo(element.destination);
        }
    }

    public clickOn(elementName: string) {
        let element = this.getElement(elementName);
        this.handleDestination(element);
        return element.locator.click()
    }

    public enterText(fieldName: string, text: string) {
        if (isUndefined(this.getCurrentPage().elements[fieldName])) {
            this.fatalError('The page: ' + this.getCurrentPage().name + ' does not have an element for ' + fieldName + '.\n');
        }
        return Promise.all([
            this.getCurrentPage().elements[fieldName].locator.clear(),
            this.getCurrentPage().elements[fieldName].locator.sendKeys(text)
        ])
    }

    public waitForPage(pageName: string) {
        let page = this.setCurrentPageTo(pageName);
        return browser.wait(function () {
            return page.at()
        }, 10 * 1000).then(function () {
        }, function (err) {
            throw 'Page: ' + pageName + ' did not appear fast enough.\n error: ' + err;
        })
    }
}
