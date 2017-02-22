/**
 * Created by barteldklasens on 2/22/17.
 */
import {Director} from "../protractor-stories/director";
import PageDictionary = require("../Pages/PageDictionary")
import PersonaDictionary = require("../Personas/PersonaDictionary")

class World {
    public director;

    constructor() {
        this.director = new Director(__dirname + '/..', PageDictionary, PersonaDictionary);
    }
}

export = function () {
    this.World = World;
};
