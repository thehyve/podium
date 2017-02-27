class AdminPersona {
    public name: string;
    public properties: {[key: string]: any};

    constructor() {
        this.name = "'THE' admin";
        this.properties = {
            "userName": "admin",
            "password": "admin",
        }

    }
}

export = new AdminPersona();
