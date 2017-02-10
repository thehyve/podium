import {Page} from "../protractor-stories/director"
import ExamplePage = require("./ExamplePage")

let PageDictionary: {[key: string]: Page} = Object.create(null);

PageDictionary['ExamplePage'] = ExamplePage;

export = PageDictionary;
