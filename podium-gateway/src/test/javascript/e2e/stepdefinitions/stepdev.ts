import {browser} from "protractor";
import {Director} from "../protractor-stories/director";
import SigninPage = require("../Pages/SigninPage")
import PageDictionary = require("../Pages/PageDictionary")
import PersonaDictionary = require("../Personas/PersonaDictionary")

let director = new Director(__dirname + '/..', PageDictionary, PersonaDictionary);

export = function () {

    this.Given(/^I go to the (.*) page$/, function (pageName, callback) {
        director.goToPage(pageName + 'Page').then(callback, callback);
    });

    this.Given(/^(.*) signs in$/, function (personaName, callback) {
        let page = director.getCurrentPage() as SigninPage;
        let persona = director.getPersona(personaName + 'Persona');
        page.login(persona.userName, persona.password).then(callback, callback);
    });

    this.Then(/^I am on the (.*) page$/, function (pageName, callback) {
        director.at(pageName + 'Page').then(callback,callback);
    });
}
