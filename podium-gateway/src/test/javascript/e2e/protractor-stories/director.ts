import {browser, ElementFinder} from "protractor";
import {Promise} from "es6-promise";
import {isUndefined} from "util";

export interface Persona {
    firstName?: string;
    lastName?: string;
    userName?: string;
    password?: string;
}

export interface Page {
    url: string;
    at?(): Promise<boolean>;
    ignoreSynchronization?: boolean;
    elements: {[name: string]: ElementFinder};
}

export class Director {
    private searchDir: string;
    private currentPage: Page;
    private currentPersona: Persona;
    private PageDictionary: {[key: string]: Page};
    private personaDictionary: {[key: string]: Persona};

    constructor(searchDir: string, PageDictionary: {[key: string]: Page}, personaDictionary: {[key: string]: Persona}) {
        this.searchDir = searchDir;
        this.PageDictionary = PageDictionary;
        this.personaDictionary = personaDictionary;
    }

    fatalError(message: string) {
        throw 'Fatal error: the specification is incorrectly expressed! ' + message;
    }

    private setCurrentPageTo(pageName: string) {
        try {
            this.currentPage = this.PageDictionary[pageName];
        } catch (error) {
            this.fatalError('The page: ' + pageName + ' does not exist.\n error: ' + error);
        }
        browser.ignoreSynchronization = this.currentPage.ignoreSynchronization == null ? false : this.currentPage.ignoreSynchronization;
        return this.currentPage
    }

    public getCurrentPage() {
        return this.currentPage;
    }

    private setCurrentPersonaTo(personaName: string) {
        try {
            return this.currentPersona = this.personaDictionary[personaName];
        } catch (error) {
            this.fatalError('The persona: ' + personaName + ' does not exist.\n error: ' + error);
        }
    }

    public getPersona(personaName: string) {
        this.setCurrentPersonaTo(personaName);
        return this.currentPersona;
    }

    public goToPage(pageName: string) {
        let page = this.setCurrentPageTo(pageName);
        return browser.get(page.url);
    }

    public at = (pageName: string) => {
        let page = this.setCurrentPageTo(pageName);
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

    private getElement(elementName: string) {
        let element = this.getCurrentPage().elements[elementName];
        if (isUndefined(element)) {
            this.fatalError(elementName + ' is not defined as an element of the active page');
        }
        return element;
    }

    public clickOn(elementName: string) {
        return this.getElement(elementName).click();
    }

    public waitForPage(pageName: string) {
        let page = this.setCurrentPageTo(pageName);
        return browser.wait(page.at
            , 10 * 1000).then(function () {
        }, function (err) {
            throw 'Page: ' + pageName + ' did not appear fast enough.\n error: ' + err;
        })
    }
}
