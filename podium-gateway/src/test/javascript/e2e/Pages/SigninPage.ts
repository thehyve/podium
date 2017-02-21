/**
 * Created by barteldklasens on 2/20/17.
 */
import {ElementFinder, $} from "protractor"
import {Promise} from 'es6-promise'

class SigninPage {
    public url: string;
    public elements: {[name:string]: ElementFinder};

    constructor() {
        this.url = "";
        this.elements = {
            "usernameInput": $('#username'),
            "passwordInput": $('#password'),
            "submitButton": $('button[type=submit]'),
        }
    }

    login(username: string, password: string){
        let that = this;
        return Promise.all([
            this.enterText('usernameInput', username),
            this.enterText('passwordInput', password)
            ]).then(function () {
                that.elements['submitButton'].click()
        })
    }

    enterText(fieldName: string, text: string){
        return Promise.all([
            this.elements[fieldName].clear(),
            this.elements[fieldName].sendKeys(text)
        ])
    }
}

export = SigninPage;
