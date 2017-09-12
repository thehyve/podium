# Podium Gateaway

The application scaffold was generated using JHipster 4.0.0, you can find documentation and help at [https://jhipster.github.io/documentation-archive/v4.0.0](https://jhipster.github.io/documentation-archive/v4.0.0).

This is a "gateway" application intended to be part of a microservice architecture, please refer to the [Doing 
microservices with JHipster](http://www.jhipster.tech/documentation-archive/v4.0.0/microservices-architecture/) page of the documentation for more information.

This application is configured for Service Discovery and Configuration with the JHipster-Registry. On launch, it will
 refuse to start if it is not able to connect to the JHipster-Registry at [http://localhost:8761](http://localhost:8761). For more information, read our documentation on [Service Discovery and Configuration with the JHipster-Registry](http://www.jhipster.tech/documentation-archive/v4.0.0/microservices-architecture/#jhipster-registry).

## Development

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

## <a href="e2e-testing"></a> End to end (e2e) testing

#### Dependencies
The end to end (e2e) tests use [Protractor](http://www.protractortest.org/#/), [Cucumber](https://github
.com/cucumber/cucumber-js) and are by default configured to use [Chrome](https://www.google.com/chrome/browser/desktop/index.html). They can only be run against a development environment.
The reason for this is that the setup steps makes use of routs from podium-uaa [TestResource]. These routes are excluded for production.

**1. Install protractor:**
~~~
yarn global add protractor
~~~

**2. Run a development [environment](../README.md#development)**

**3. Run tests**
~~~
#navigate to the test folder
cd podium-gateway/src/test/javascript/

#run all tests
protractor

#run tests for a specific feature file
protractor --specs=e2e/features/bbmri-admin-organisations.feature
~~~

#### Adding tests
Test are made up out of three main components.
- A scenario described in a feature file. `e2e/features/*.feature`
- Step definitions `e2e/stepdefinitions/*.ts`
- Test data `e2e/pages/*.ts` `e2e/data/data-dictionary.ts` `e2e/personas/persona-dictionary.ts`

The director class is used to bind the test data to test scripts. dataIds that are in .feature files can be used to 
retrieve test data from the appropriate dictionary.
for examples on how to do this look at the .ts files in `e2e/stepdefinitions/*.ts`
