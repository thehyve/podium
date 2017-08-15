# bbmri-podium-gateway

### <a href="e2e-testing"></a> End to end testing

####Dependencies
The end to end tests use [Protractor](http://www.protractortest.org/#/), [Cucumber](https://github.com/cucumber/cucumber-js) and are by default configured to use [Chrome](https://www.google.com/chrome/browser/desktop/index.html). They can only be run against a development environment.
The reason for this is that the setup steps makes use of routs from podium-uaa [TestResource]. These routes are excluded for production.

Install protractor:
~~~
yarn global add protractor
~~~

run a dev [environment]()

run tests
~~~
#navigate to the test folder
cd podium-gateway/src/test/javascript/

#run all tests
protractor

#run tests for a specific feature file
protractor --specs=e2e/features/bbmri-admin-organisations.feature
~~~

####adding tests
Test are made up out of three main components.
- A scenario described in a feature file. `e2e/features/*.feature`
- Step definitions `e2e/stepdefinitions/*.ts`
- Test data `e2e/pages/*.ts` `e2e/data/data-dictionary.ts` `e2e/personas/persona-dictionary.ts`

The director class is used to bind the test data to test scripts. dataIds that are in .feature files can be used to 
retrieve test data from the appropriate dictionary.
for examples on how to do this look at the .ts files in `e2e/stepdefinitions/*.ts`
