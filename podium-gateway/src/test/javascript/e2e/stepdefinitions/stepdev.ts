import {browser, $} from "protractor";
import {Director} from "../protractor-stories/director";
import {Promise} from "es6-promise";
import SigninPage = require("../pages/SigninPage")
import PageDictionary = require("../pages/PageDictionary")
import PersonaDictionary = require("../personas/PersonaDictionary")

let director = new Director(__dirname + '/..', PageDictionary, PersonaDictionary);

export = function () {

    this.Given(/^I go to the (.*) page$/, function (pageName, callback) {
        director.goToPage(pageName + 'Page').then(callback, callback);
    });

    this.Given(/^(.*) signs in$/, function (personaName, callback) {
        let persona = director.getPersona(personaName + 'Persona');
        Promise.all([
            director.enterText('usernameInput', persona.properties['userName']),
            director.enterText('passwordInput', persona.properties['password'])
        ]).then(function () {
            director.clickOn('submitButton')
        }).then(callback, callback);
    });

    this.Then(/^I am on the (.*) page$/, function (pageName, callback) {
        director.at(pageName + 'Page').then(callback, callback);
    });
}
