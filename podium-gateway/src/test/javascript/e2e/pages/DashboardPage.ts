/**
 * Created by barteldklasens on 2/20/17.
 */
import {browser, $} from "protractor";
import {Interactable} from "../protractor-stories/director";

class DashboardPage {
    public url: string;
    public elements: {[name: string]: Interactable};

    public at() {
        let that = this
        return browser.getCurrentUrl().then(function (currentUrl) {
            return (browser.baseUrl + that.url) == currentUrl;
        })
    }

    constructor() {
        this.url = "#/dashboard";
        this.elements = {
            "submitButton": {locator: $('span[ng-reflect-inner-h-t-m-l]')},
        }
    }
}

export = DashboardPage;
