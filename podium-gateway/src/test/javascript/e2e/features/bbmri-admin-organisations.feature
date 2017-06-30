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
            | ShortName | SomeBank, VarnameBank, XBank |

    @default
    Scenario: An organisation has a details page
        Given BBMRI_Admin goes to the 'organisation details' page for the organisation 'VarnameBank'
        Then the organisation details page contains 'VarnameBank's data

#    Scenario: An organisation's details can be edited
#        Given Rob goes to the 'organisation' organisation details page
#        When 'he' edits the details: {fieldName: value}
#        Then the new details are saved

#    Scenario: BBMRI admins can page through lists of users
#        Given Rob goes to the 'organisation overview' page
#        When 'he' goes to the next list of organisations
#        Then the list of organisations has changed

#    Scenario: BBMRI admins search for organisations by name
#        Given Rob goes to the 'organisation overview' page
#        When 'he' searches for 'organisationName'
#        Then only 'organisationName' is shown in the list

    @default
    Scenario: BBMRI admins can create new organisations
        Given BBMRI_Admin goes to the 'create organisation' page
        When he creates the organisation 'NewOrg'
        Then 'NewOrg' organisation exists

#    Scenario: BBMRI admins can deactivate organisations
#        Given Rob goes to the 'organisation overview' page
#        When 'he' deactivates the organisation 'organisationName'
#        Then 'that' organisation is Deactivated
