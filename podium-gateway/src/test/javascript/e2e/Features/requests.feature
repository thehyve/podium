Feature: The main function of podium is for researchers to make requests to organizations (BRPREQ-121)

    Scenario: A researcher is able to save incomplete request at all times as a draft
        Given 'Simone' is on the 'request overview' page
        When 'she' creates a new draft filling data for 'requestName'
        Then the draft is saved

    Scenario: A researcher is able to turn a draft in to a request
        Given 'Simone' is on the 'request overview' page
        When 'she' submits the draft 'requestName' as a request
        Then the request is created
        And the draft is removed

    Scenario: A researcher is able to create a request
        Given 'Simone' is on the 'request overview' page
        When 'she' creates a new request filling data for 'requestName'
        Then the request is created
