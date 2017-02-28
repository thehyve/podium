Feature: To test if the setup works

    Scenario: test setup
        Given I go to the Signin page
        When Admin signs in
        Then I am on the Dashboard page
