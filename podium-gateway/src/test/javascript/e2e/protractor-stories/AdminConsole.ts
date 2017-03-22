/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import request = require('request')

export class AdminConsole {
    public token: string;

    constructor() {
    }

    public authenticate(callback) {//store token first time
        let that = this;
        let options = {
            method: 'POST',
            url: 'http://localhost:8080/podiumuaa/oauth/token',
            headers: {
                'Authorization': 'Basic d2ViX2FwcDo='
            },
            formData: {
                grant_type: "password",
                username: "admin",
                password: "admin"
            }
        };

        request(options, callback)
    }


    public checkUser(persona, check, callback) {

        this.authenticate(function (error, response, body) {
            let options = {
                method: 'GET',
                url: 'http://localhost:8080/podiumuaa/api/users/' + persona.properties.userName,
                headers: {
                    'Authorization': 'Bearer ' + JSON.parse(body).access_token
                }
            };
            request(options, function (error, response, body) {
                let user = JSON.parse(body);
                if (check(persona, user)) {
                    callback()
                } else {
                    callback("check failed")
                }
            })
        });
    }

    //currently there is no way to verify a new user
    public registerUser(persona, callback){
        this.authenticate(function (error, response, body) {
            let options = {
                method: 'POST',
                url: 'http://localhost:8080/podiumuaa/api/users/',
                headers: {
                    'Authorization': 'Bearer ' + JSON.parse(body).access_token
                },
                body: JSON.stringify(
                    {
                        "id": null,
                        "uuid": null,
                        "login": persona.properties.userName,
                        "firstName": persona.properties.firstName,
                        "lastName": persona.properties.lastName,
                        "email": persona.properties.email,
                        "telephone": persona.properties.telephone,
                        "institute": persona.properties.institute,
                        "department": persona.properties.department,
                        "jobTitle": persona.properties.jobTitle,
                        "specialism": persona.properties.specialism,
                        "emailVerified": false,
                        "adminVerified": false,
                        "accountLocked": false,
                        "langKey": "en",
                        "authorities": [
                            "ROLE_RESEARCHER"
                        ],
                        "createdBy": null,
                        "createdDate": null,
                        "lastModifiedBy": null,
                        "lastModifiedDate": null,
                        "password": persona.properties.password
                    }
                )
            };
            request(options, function (error, response, body) {
                console.log(response);
                // callback();
            })
        });
    }

    public verifyUser(){

    }

    public deleteUser(persona, callback){
        this.authenticate(function (error, response, body) {
            let options = {
                method: 'DELETE',
                url: 'http://localhost:8080/podiumuaa/api/users/' + persona.properties.userName,
                headers: {
                    'Authorization': 'Bearer ' + JSON.parse(body).access_token
                }
            };
            request(options, function (error, response, body) {
                console.log(response);
                // callback();
            })
        });
    }

    public unlockUser(persona){
        let token
        this.authenticate(function (error, response, body) {
            token = JSON.parse(body).access_token
            let options = {
                method: 'GET',
                url: 'http://localhost:8080/podiumuaa/api/users/' + persona.properties.userName,
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            };
            request(options, function (error, response, body) {
                let user = JSON.parse(body);

                let options = {
                    method: 'PUT',
                    url: 'http://localhost:8080/podiumuaa/api/users/uuid/'+ user.uuid +'/unlock',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                };
                request(options)
            })
        });
    }
}
