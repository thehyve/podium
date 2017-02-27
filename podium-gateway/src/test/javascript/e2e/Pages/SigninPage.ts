/**
 * Created by barteldklasens on 2/20/17.
 */
import {$} from "protractor";
import {Interactable} from "../protractor-stories/director";

class SigninPage {
    public url: string;
    public elements: {[name: string]: Interactable};

    constructor() {
        this.url = "";
        this.elements = {
            "usernameInput": {locator: $('#username')},
            "passwordInput": {locator: $('#password')},
            "submitButton": {locator: $('button[type=submit]'), destination: 'DashboardPage', strict: true},
        }
    }
}

export = SigninPage;
