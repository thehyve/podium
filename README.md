[![Build Status](https://travis-ci.org/thehyve/podium.svg?branch=master)](https://travis-ci.org/thehyve/podium)
[![Build Status](https://travis-ci.org/thehyve/podium.svg?branch=dev)](https://travis-ci.org/thehyve/podium)

<img src="https://github.com/thehyve/podium/blob/master/assets/browserstack-logo.png?raw=true" style="display:inline;" width="200" height="105">

# Welcome to the Podium Request Portal
Podium is the request portal for samples, data and images from biobanks with the purpose to uniformize the request 
and review processes for all associated users and organisations.

The application scaffold was generated using JHipster 4.0.0, you can find documentation and help at [https://jhipster.github.io/documentation-archive/v4.0.0](https://jhipster.github.io/documentation-archive/v4.0.0).


### Setup database

For development, you can create a Postgres database locally with `psql`:
`sudo -u postgres psql`
```sql
create role "podiumUser" with password 'podiumUser' login;
create database "podiumUaa";
create database "podiumGateway";
grant all on database "podiumUaa" to "podiumUser";
grant all on database "podiumGateway" to "podiumUser";
```

### Dependencies

Before you can build this project, you must install and configure the following dependencies on your machine:
1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.
2. [Yarn][]: We use Yarn to manage Node dependencies.
   Depending on your system, you can install Yarn either from source or as a pre-packaged bundle.

### Microservices Overview

Podium is built in a microservices architecture which works in following way:

* Podium Gateway, serves the Angular application and also handles web traffic.
* Podium UAA, is a User Account and Authentication server based on Spring Security. This server provides OAuth2 tokens for securing the gateway.
* Podium Registry, a runtime application on which all applications registers and get their configuration from. It also provides runtime monitoring dashboards.

The services have to be started in the following order:

1. Podium Registry
2. Podium UAA
3. Podium Gateway

## Development

First everything should be installed by running the following in the root folder.

    $ podium > mvn clean install

#### Running Podium Registry
Before the uaa and gateway services can start [Podium Registry][] needs to be up and running.

#### Running Podium UAA
After that open a new terminal and navigate to the podium-uaa folder and start the uaa service with 

    $ podium/podium-uaa > mvn

#### Running Podium Gateway
[Yarn][] is used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in `package.json`. You can also run `yarn update` and `yarn install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `yarn help update`.

The `yarn run` command will list all of the scripts available to run for this project.

Finally open two more terminals and run the following commands in the `/podium-gateway` folder to create a blissful development experience where your browser auto-refreshes when files change on your hard drive.

    $ podium/podium-gateway > mvn
    $ podium/podium-gateway > yarn start


## <a href="building-for-production"></a> Building for production

To optimize the podiumGateway application for production, run:

    mvn -Pprod clean package

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

    java -jar target/*.war

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

## <a href="testing"></a> Testing

To launch a service's tests, run in its folder:

    mvn clean test

### <a href="testing-client"></a>Client tests

Unit tests are run by [Karma][] and written with [Jasmine][]. They're located in `src/test/javascript/` 
in each of the service folders and can be run with:

    yarn test

For UI end-to-end tests see the READMEs of the individual components

### <a href="testing-performance"></a>Performance tests

Performance tests are run by [Gatling][] and written in Scala. They're located in `src/test/gatling` 
in each of the service folders and can be run with:

    mvn gatling:execute

For more information, refer to the [Running tests page][].

## <a href="continuous-integration"></a>Continuous Integration (optional)

To set up a CI environment, consult the [Setting up Continuous Integration][] page.

## Wiki

For more information please [see the wiki](https://github.com/thehyve/podium/wiki)

[JHipster Homepage and latest documentation]: https://jhipster.github.io
[JHipster 4.0.0 archive]: https://podium.github.io/documentation-archive/v4.0.0
[Setting up Continuous Integration]: https://jhipster.github.io/documentation-archive/v4.0.0/setting-up-ci/

[Gatling]: http://gatling.io/
[Node.js]: https://nodejs.org/
[Yarn]: https://yarnpkg.org/
[Webpack]: https://webpack.github.io/
[Karma]: http://karma-runner.github.io/
[Jasmine]: http://jasmine.github.io/2.0/introduction.html
[Protractor]: https://angular.github.io/protractor/
[Leaflet]: http://leafletjs.com/
[DefinitelyTyped]: http://definitelytyped.org/
[Podium Registry]: https://github.com/thehyve/podium-registry
[Npm]: https://www.npmjs.com/
[Running tests page]: http://www.jhipster.tech/running-tests/
