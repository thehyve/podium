Feature: bbmri admins can create and manage organizations in the system (BRPREQ-37, BRPREQ-30)

    Scenario: BBMRI admins can see organizations in the system
        Given 'Rob' is on the 'organization overview' page
        Then the overview contains the organization\'s 'name, shortName, adminNames, coordinatorNames' for the organizations '[names]'

    Scenario Outline: user overview page is sortable by name
        Given 'Rob' is on the 'organization overview' page
        When 'he' sorts by '<sortBy>'
        Then organizations are displayed in the following order: '<organizationOrder>'

        Examples:
            | sortBy           | organizationOrder |
            | nothing          |                   |
            | name             |                   |
            | shortName        |                   |
            | adminNames       |                   |
            | coordinatorNames |                   |

    Scenario: An organization has a details page
        Given 'Rob' is on the 'organization overview' page
        When 'he' opens the details for 'organization'
        Then 'organization details' contains: {fieldName: value}

    Scenario: An organization's details can be edited
        Given 'Rob' is on the 'organization' organization details page
        When 'he' edits the details: {fieldName: value}
        Then the new details are saved

    Scenario: BBMRI admins can page through lists of users
        Given 'Rob' is on the 'organization overview' page
        When 'he' goes to the next list of organizations
        Then the list of organizations has changed

    Scenario: BBMRI admins search for organizations by name
        Given 'Rob' is on the 'organization overview' page
        When 'he' searches for 'organizationName'
        Then only 'organizationName' is shown in the list

    Scenario: BBMRI admins can create new organizations
        Given 'Rob' is on the 'organization overview' page
        When 'he' creates the organization 'organizationName'
        Then 'that' organization is created

    Scenario: BBMRI admins can deactivate organizations
        Given 'Rob' is on the 'organization overview' page
        When 'he' deactivates the organization 'organizationName'
        Then 'that' organization is Deactivated
