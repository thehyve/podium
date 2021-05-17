[![Build Status](https://travis-ci.org/thehyve/podium.svg?branch=dev)](https://travis-ci.org/thehyve/podium)
[![codebeat badge](https://codebeat.co/badges/f225e930-5ea9-4cd0-95cd-9cf7a17169ed)](https://codebeat.co/projects/github-com-thehyve-podium-master)
[![codecov](https://codecov.io/gh/thehyve/podium/branch/dev/graph/badge.svg)](https://codecov.io/gh/thehyve/podium)


# Welcome to the Podium request portal

**Podium** is the request portal for samples, data and images from biobanks
with the purpose to uniformize the request and review processes
for all associated users and organisations.



## Microservices overview

Podium is built in a microservices architecture which works in following way:

1. **Podium Registry**<br>
The registry is a runtime application on which all applications registers and get their configuration from.
It also provides runtime monitoring dashboards.
The registry application is available in the [Podium Registry] repository. 

2. **Podium UAA**<br>
Podium UAA is  a user accounting and authorizing service for securing microservices using the OAuth2 
authorization protocol. Podium UAA is an fully configured OAuth2 authorization server with the users and roles 
endpoints inside, wrapped into a usual JHipster application. This allows the developer to deeply configure every aspect 
of his user domain, without restricting on policies by other ready-to-use UAAs.

3. **Podium Gateway**<br>
The gateway serves the Angular frontend application, contains a component
for handling requests and deliveries, and also handles web traffic

The services have to be started in order.



## Development

### Database setup

For development, you can create a Postgres database locally with `psql`:
`sudo -u postgres psql`
```sql
create role "podiumUser" with password 'podiumUser' login;
create database "podiumUaaDev";
create database "podiumGatewayDev";
grant all on database "podiumUaaDev" to "podiumUser";
grant all on database "podiumGatewayDev" to "podiumUser";
```

### Dependencies

Before you can build this project, you must install and configure the following dependencies on your machine:
1. [Maven]: Maven is used to build the microservices and to publish them to a Nexus repository.
1. [Node.js]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.
2. [Yarn]: We use Yarn to manage Node dependencies.
   Depending on your system, you can install Yarn either from source or as a pre-packaged bundle.

Java dependencies are managed by Maven in the `pom.xml` files. Versions of packages are
configured in the root [pom.xml](pom.xml).
[Yarn] is used to manage CSS and JavaScript dependencies for the user interface application.
You can upgrade dependencies by specifying a newer version in [package.json](podium-gateway/package.json).
You can use `npm install`, `yarn update` and `yarn install` to manage dependencies.

### Install

First everything should be installed by running the following in the root folder:

```bash
mvn clean install
```

### Run

1. **Start Podium Registry**<br>
[Podium Registry] needs to be up and running before the UAA and Gateway services start.
Go to your `podium-registry` folder and start the registry with `mvn`.
The user interface of the registry will be available at [http://localhost:8761](http://localhost:8761).

2. **Start ElasticSearch**
Run `docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.8.15`.

3. **Start Podium UAA**<br>
Open a new terminal and navigate to the `podium-uaa` folder and start the UAA service with 
`mvn`. The UAA service will be listening to port 9999.

4. **Start Podium Gateway**<br>
Open a new terminal and run the following commands in the `podium-gateway` folder:
```bash
# start the podium gateway service
mvn
```
The application is now available at [http://localhost:8080](http://localhost:8080).

To create a blissful development experience, where your browser auto-refreshes when files change on your hard drive,
you can start a hot reloading user interface with:
```bash
# serve the user interface for hot reloading
npm run start
```
This will open the application at [http://localhost:9000](http://localhost:9000) in your browser.

### Building for production

To optimize the podiumGateway application for production, run:
```bash
# Build for production
mvn -DskipTests -Pprod clean package

# Publish to Nexus
mvn -DskipTests -Pprod clean deploy
```
This will concatenate and minify the client CSS and JavaScript files.
It will also modify `index.html` so it references these new files.
To ensure everything worked, run:
```bash
    java -jar target/*.war
```
Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.



## Installation

For installing Podium for production use, there is an [installation manual](docs/installation.md).



## Testing

### Microservice tests

To launch a service's tests, run in its folder:
```bash
mvn -Dspring.profiles.active=h2,test test
```
Tests are run automatically on [Travis](https://travis-ci.org/thehyve/podium/branches).

### User interface tests

User interface unit tests are run by [Karma] and written with [Jasmine].
They're located in `src/test/javascript/` in each of the service folders and can be run with:

```bash
npm run test
```

### End to end (e2e) testing

#### Dependencies

The end to end (e2e) tests use [Protractor], [Cucumber] and are by default configured to use Chrome.
They can only be run against a development environment.
The reason for this is that the setup steps makes use of routes from podium-uaa [TestResource]. These routes are excluded for production.

**1. Install protractor:**
```bash
npm install --global protractor
```

**2. Run a development [environment](#development)**

**3. Run tests**
```bash
# Navigate to the test folder
cd podium-gateway/src/test/javascript/

# Run all tests
protractor

# Run tests for a specific feature file
protractor --specs=e2e/features/bbmri-admin-organisations.feature
```

By default the test suite expects the user interface to be available at port 9000.
To change this, set the `baseUrl` setting in [protractor.conf.js](podium-gateway/src/test/javascript/protractor.conf.js).

#### Adding tests
Test are made up out of three main components.
- A scenario described in a feature file. `e2e/features/*.feature`
- Step definitions `e2e/stepdefinitions/*.ts`
- Test data `e2e/pages/*.ts` `e2e/data/data-dictionary.ts` `e2e/personas/persona-dictionary.ts`

The director class is used to bind the test data to test scripts. dataIds that are in .feature files can be used to 
retrieve test data from the appropriate dictionary.
for examples on how to do this look at the .ts files in `e2e/stepdefinitions/*.ts`



## License

Copyright &copy; 2017, 2018 &nbsp; The Hyve and respective contributors.

This program is free software: you can redistribute it and/or modify
it under the terms of the Apache 2.0 License
published by the Apache Software Foundation, either version 2.0 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
Apache 2.0 License for more details.

You should have received a copy of the [Apache 2.0 License](al-2.0.txt)
along with this program. If not, see
https://www.apache.org/licenses/LICENSE-2.0.txt.


[JHipster Homepage and latest documentation]: https://jhipster.github.io
[JHipster 4.0.0 archive]: https://podium.github.io/documentation-archive/v4.0.0
[Setting up Continuous Integration]: https://jhipster.github.io/documentation-archive/v4.0.0/setting-up-ci/

[Maven]: https://maven.apache.org/
[Node.js]: https://nodejs.org/
[Npm]: https://www.npmjs.com/
[Yarn]: https://yarnpkg.org/
[Webpack]: https://webpack.github.io/
[Karma]: http://karma-runner.github.io/
[Jasmine]: http://jasmine.github.io/2.0/introduction.html
[Protractor]: https://www.protractortest.org/
[Cucumber]: https://github.com/cucumber/cucumber-js
[Leaflet]: http://leafletjs.com/
[DefinitelyTyped]: http://definitelytyped.org/

[Podium Registry]: https://github.com/thehyve/podium-registry
[TestResource]: https://github.com/thehyve/podium/blob/master/podium-uaa/src/main/java/nl/thehyve/podium/web/rest/TestResource.java
