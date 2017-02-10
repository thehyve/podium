import {Persona} from "../protractor-stories/director"
import ExamplePersona = require("./ExamplePersona")

let PersonaDictionary: {[key: string]: Persona} = Object.create(null);

PersonaDictionary['ExamplePersona'] = ExamplePersona;


export = PersonaDictionary;
