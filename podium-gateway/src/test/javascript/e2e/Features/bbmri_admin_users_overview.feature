Feature: For a BBMRI admin to do his or her work they need information from the system (BRPREQ-32).

    Scenario: BBMRI admins can see users in the system
        Given 'Rob' is on the 'user overview' page
        Then the overview contains the user\'s 'name, email, role, accountStatus' for the users '[names]'

    Scenario Outline: user overview page is sortable by name
        Given 'Rob' is on the 'user overview' page
        When 'he' sorts by '<sortBy>'
        Then users are displayed in the following order: '<userOrder>'

        Examples:
            | sortBy        | userOrder |
            | nothing       |           |
            | name          |           |
            | email         |           |
            | role          |           |
            | accountStatus |           |

    Scenario: An user has a details page
        Given 'Rob' is on the 'user overview' page
        When 'he' opens the details for 'user'
        Then 'user details' contains: {fieldName: value}

    Scenario: An user's details can be edited
        Given 'Rob' is on the 'user' user details page
        When 'he' edits the details: {fieldName: value}
        Then the new details are saved

    Scenario: deleted users are deactivated
        Given 'Rob' is on the 'user overview' page
        When 'he' deletes 'user'\'s account
        Then 'user'\'s account has the status 'deactivated'

    Scenario: BBMRI admins can page through lists of users
        Given 'Rob' is on the 'user overview' page
        When 'he' goes to the next list of users
        Then the list of users has changed

