[![Build Status](https://travis-ci.org/thehyve/podium.svg?branch=master)](https://travis-ci.org/thehyve/podium)
[![Build Status](https://travis-ci.org/thehyve/podium.svg?branch=dev)](https://travis-ci.org/thehyve/podium)

<img src="https://github.com/thehyve/podium/assets/browserstack-logo.png" style="display:inline;" width="200" height="60">

# Welcome to the Podium Request Portal
Podium is the request portal for samples, data and images from BBMRI Biobanks, with the purpose to uniformize the request and review processes for all associated users and organisations.

The application scaffold was generated using JHipster 4.0.0, you can find documentation and help at [https://jhipster.github.io/documentation-archive/v4.0.0](https://jhipster.github.io/documentation-archive/v4.0.0).

This application is configured for Service Discovery and Configuration with the Podium-Registry. On launch, it will refuse to start if it is not able to connect to the Podium-Registry at [http://localhost:8761](http://localhost:8761).


## What is Podium?

## <a href="development"></a>Development

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

After installing Node, you should be able to run the following command to install development tools.
You will only need to run this command when dependencies change in `package.json`.

    yarn install

We use npm scripts and [Webpack][] as our build system.

Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

    mvn
    yarn start

[Npm][] is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in `package.json`. You can also run `yarn update` and `yarn install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `yarn help update`.

The `yarn run` command will list all of the scripts available to run for this project.

### <a href="dependencies"></a>Adding dependencies

For example, to add [Leaflet][] library as a runtime dependency of your application, you would run following command:

    yarn add --exact leaflet

To benefit from TypeScript type definitions from [DefinitelyTyped][] repository in development, you would run following command:

    yarn add --dev --exact @types/leaflet

Then you would import the JS and CSS files specified in library's installation instructions so that [Webpack][] knows about them:

Edit `src/main/webapp/app/vendor.ts`file:
~~~
import 'leaflet/dist/leaflet.js';
~~~

Edit `src/main/webapp/content/css/vendor.css` file:
~~~
@import '~leaflet/dist/leaflet.css';
~~~

Note: there are still few other things remaining to do for Leaflet that we won't detail here.

### <a href="building-for-production"></a> Building for production

To optimize the podiumGateway application for production, run:

    mvn -Pprod clean package

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

    java -jar target/*.war

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

### <a href="testing"></a> Testing

To launch your application's tests, run:

    mvn clean test

#### <a href="testing-client"></a>Client tests

Unit tests are run by [Karma][] and written with [Jasmine][]. They're located in `src/test/javascript/` and can be run with:

    yarn test

UI end-to-end tests are powered by [Protractor][], which is built on top of WebDriverJS. They're located in `src/test/javascript/e2e`
and can be run by starting Spring Boot in one terminal (`./mvnw spring-boot:run`) and running the tests (`yarn e2e`) in a second one.

#### <a href="testing-performance"></a>Performance tests

Performance tests are run by [Gatling][] and written in Scala. They're located in `src/test/gatling` and can be run with:

    mvn gatling:execute

For more information, refer to the [Running tests page][].

### <a href="docker"></a>Using Docker to simplify development (optional)

You can use Docker to improve your Podium development experience. A number of docker-compose configuration are available in the `src/main/docker` folder to launch required third party services.
For example, to start a postgresql database in a docker container, run:

    docker-compose -f src/main/docker/postgresql.yml up -d

To stop it and remove the container, run:

    docker-compose -f src/main/docker/postgresql.yml down

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

    mvn package -Pprod docker:build

Then run:

    docker-compose -f src/main/docker/app.yml up -d

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the docker-compose sub-generator (`yo podium:docker-compose`), which is able to generate docker configurations for one or several JHipster applications.

### <a href="continuous-integration"></a>Continuous Integration (optional)

To set up a CI environment, consult the [Setting up Continuous Integration][] page.

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
