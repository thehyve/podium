import {Page} from "../protractor-stories/director";
import SigninPage = require("./SigninPage")
import DashboardPage = require("./DashboardPage")

let PageDictionary: {[key: string]: Page} = Object.create(null);

PageDictionary['SigninPage'] = new SigninPage;
PageDictionary['DashboardPage'] = new DashboardPage;

export = PageDictionary;
