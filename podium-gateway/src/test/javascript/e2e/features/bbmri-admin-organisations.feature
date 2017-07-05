Feature: bbmri admins can create and manage organisations in the system (BRPREQ-37, BRPREQ-30)

    @default
    Scenario: BBMRI admins can see organisations in the system
        Given BBMRI_Admin goes to the 'organisation overview' page
        When he sorts by 'Name'
        Then the overview contains the organisation's 'name, shortName' for the organisations 'XBank, SomeBank, VarnameBank'

    @default
    Scenario Outline: user overview page is sortable by <sortBy>
        Given BBMRI_Admin goes to the 'organisation overview' page
        When he sorts by '<sortBy>'
        Then organisations are displayed in the following order: '<organisationOrder>'

        Examples:
            | sortBy    | organisationOrder            |
            | Name      | XBank, SomeBank, VarnameBank |
            | ShortName | XBank, VarnameBank, SomeBank |

    @default
    @request
    Scenario: An organisation has a details page
        Given BBMRI_Admin goes to the 'organisation details' page for the organisation 'ImageBank'
        Then the organisation details page contains 'ImageBank's data

    @default
    Scenario: BBMRI admins can create new organisations
        Given BBMRI_Admin goes to the 'create organisation' page
        When he creates the organisation 'NewOrg'
        Then 'NewOrg' organisation exists

    @default
    Scenario: BBMRI admins can deactivate organisations
        Given BBMRI_Admin goes to the 'organisation overview' page
        When he deactivates the organisation 'VarnameBank'
        Then the organisation is Deactivated

    @default
    Scenario: BBMRI admins can edit organisation details
        Given BBMRI_Admin goes to the 'organisation details' page for the organisation 'VarnameBank'
        When he indicates that 'Data, Images' can be requested from 'VarnameBank'
        Then the organisation's data has changed

