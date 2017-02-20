import {browser} from "protractor";

export interface Persona {
    firstName?: string;
    lastName?: string;
    userName?: string;
    password?: string;
}

export interface Page {
    url: string;
    at?(): boolean;
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

    private setCurrentPersonaTo(personaName: string) {
        try {
            console.log(this.personaDictionary);
            console.log(this.personaDictionary[personaName]);
            return this.currentPersona = this.personaDictionary[personaName];
        } catch (error) {
            this.fatalError('The persona: ' + personaName + ' does not exist.\n error: ' + error);
        }
    }

    //public API
    public goToPage(pageName: string) {
        let page = this.setCurrentPageTo(pageName);
        browser.ignoreSynchronization = page.ignoreSynchronization == null ? false : page.ignoreSynchronization;
        return browser.get(page.url);
    }

    public testPersona(personaName: string){
        let persona = this.setCurrentPersonaTo(personaName);
        console.log(persona.firstName);
    }
}
