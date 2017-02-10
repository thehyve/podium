import {browser} from "protractor";
import {Director} from "../protractor-stories/director";
import PageDictionary = require("../Pages/PageDictionary")
import PersonaDictionary = require("../Personas/PersonaDictionary")

let director = new Director(__dirname + '/..', PageDictionary, PersonaDictionary);

export = function () {

    this.Given(/^I run e2e$/, function (callback) {
        director.goToPage('ExamplePage');
        callback();
    });

    this.When(/^I cross my fingers$/, function (callback) {
        director.testPersona('ExamplePersona');
        callback();
    });

    this.Then(/^I am happy everything runs$/, function (callback) {
        callback();
    });
}
