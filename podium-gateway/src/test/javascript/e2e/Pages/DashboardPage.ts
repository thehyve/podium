/**
 * Created by barteldklasens on 2/20/17.
 */
import {ElementFinder, $} from "protractor"
import {browser} from "protractor";

class DashboardPage {
    public url: string;
    public elements: {[name:string]: ElementFinder};
    public at(){
        let that = this
        return browser.getCurrentUrl().then(function(currentUrl){
            return (browser.baseUrl + that.url) == currentUrl;
        })
    }

    constructor() {
        this.url = "#/dashboard";
        this.elements = {
            "submitButton": $('span[ng-reflect-inner-h-t-m-l]'),
        }
    }
}

export = DashboardPage;
