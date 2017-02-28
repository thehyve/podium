import {Persona} from "../protractor-stories/director";
import AdminPersona = require("./AdminPersona")

let PersonaDictionary: {[key: string]: Persona} = Object.create(null);

PersonaDictionary['AdminPersona'] = AdminPersona;


export = PersonaDictionary;
