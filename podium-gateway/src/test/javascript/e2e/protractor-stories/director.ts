import {browser} from "protractor";
import {Promise} from 'es6-promise'

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
            return this.currentPage = this.PageDictionary[pageName];
        } catch (error) {
            this.fatalError('The page: ' + pageName + ' does not exist.\n error: ' + error);
        }
    }

    public getCurrentPage(){
        return this.currentPage;
    }

    private setCurrentPersonaTo(personaName: string) {
        try {
            return this.currentPersona = this.personaDictionary[personaName];
        } catch (error) {
            this.fatalError('The persona: ' + personaName + ' does not exist.\n error: ' + error);
        }
    }

    public getPersona(personaName: string){
        this.setCurrentPersonaTo(personaName);
        return this.currentPersona;
    }

    //public API
    public goToPage(pageName: string) {
        let page = this.setCurrentPageTo(pageName);
        browser.ignoreSynchronization = page.ignoreSynchronization == null ? false : page.ignoreSynchronization;
        return browser.get(page.url);
    }

    public at(pageName: string){
        let page = this.setCurrentPageTo(pageName);
        return Promise.resolve(page.at()).then(function(v) {
            return new Promise(function(resolve, reject) {
                if (v) {
                    resolve();
                }
                else {
                    reject(Error('not at page: ' + pageName));
                }
            })
        });
    }
}
