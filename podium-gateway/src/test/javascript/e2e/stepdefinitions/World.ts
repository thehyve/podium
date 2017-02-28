/**
 * Created by barteldklasens on 2/22/17.
 */
import {Director} from "../protractor-stories/director";
import PageDictionary = require("../pages/PageDictionary")
import PersonaDictionary = require("../personas/PersonaDictionary")

class World {
    public director;

    constructor() {
        this.director = new Director(__dirname + '/..', PageDictionary, PersonaDictionary);
    }
}

export = function () {
    this.World = World;
};
