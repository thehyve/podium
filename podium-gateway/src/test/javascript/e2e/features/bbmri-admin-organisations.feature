Feature: bbmri admins can create and manage organizations in the system (BRPREQ-37, BRPREQ-30)

    @default
    Scenario: BBMRI admins can see organizations in the system
        Given BBMRI_Admin goes to the 'organization overview' page
        When he sorts by 'Name'
        Then the overview contains the organization's '["name", "shortName"]' for the organizations '["XBank", "SomeBank", "VarnameBank"]'

    @default
    Scenario Outline: user overview page is sortable by <sortBy>
        Given BBMRI_Admin goes to the 'organization overview' page
        When he sorts by '<sortBy>'
        Then organizations are displayed in the following order: '<organizationOrder>'

        Examples:
            | sortBy    | organizationOrder                     |
#            | Nothing   | ["VarnameBank", "SomeBank", "XBank"]  |
            | Name      | ["XBank", "SomeBank", "VarnameBank"]  |
            | ShortName | ["SomeBank", "VarnameBank",  "XBank"] |
#            | adminNames       |                   |
#            | coordinatorNames |                   |

    @default
    Scenario: An organization has a details page
        Given BBMRI_Admin goes to the organization details page for 'VarnameBank'
        Then the organization details page contains 'VarnameBank's data

#    Scenario: An organization's details can be edited
#        Given Rob goes to the 'organization' organization details page
#        When 'he' edits the details: {fieldName: value}
#        Then the new details are saved

#    Scenario: BBMRI admins can page through lists of users
#        Given Rob goes to the 'organization overview' page
#        When 'he' goes to the next list of organizations
#        Then the list of organizations has changed

#    Scenario: BBMRI admins search for organizations by name
#        Given Rob goes to the 'organization overview' page
#        When 'he' searches for 'organizationName'
#        Then only 'organizationName' is shown in the list

    @default
    Scenario: BBMRI admins can create new organizations
        Given BBMRI_Admin goes to the 'create organisation' page
        When he creates the organization 'NewOrg'
        Then 'NewOrg' organization exists

#    Scenario: BBMRI admins can deactivate organizations
#        Given Rob goes to the 'organization overview' page
#        When 'he' deactivates the organization 'organizationName'
#        Then 'that' organization is Deactivated
