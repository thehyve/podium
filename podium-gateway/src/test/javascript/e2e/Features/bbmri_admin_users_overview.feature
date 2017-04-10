Feature: For a BBMRI admin to do his or her work they need information from the system (BRPREQ-32).

    @default
    Scenario: BBMRI admins can see users in the system
        Given Rob goes to the 'user management' page
        Then the overview contains the user's '["login", "email", "emailVerified", "adminVerified", "authority"]' for the users '["System", "Admin", "BBMRI_Admin"]'

    @default
    Scenario Outline: user management page is sortable by <sortBy>
        Given Rob goes to the 'user management' page
        When he sorts by '<sortBy>'
        Then users are displayed in the following order: '<userOrder>'

        Examples:
            | sortBy | userOrder                                           |
#            | Nothing | ["System", "Admin", "BBMRI_Admin"] |
            | Login  | ["Admin", "BBMRI_Admin", "Linda", "Dave", "System"] |
            | Email  | ["Admin", "BBMRI_Admin", "Dave", "Linda", "System"] |
#            | Role          |                                    |
#            | AccountStatus |                                    |

    @default
    Scenario: An user has a details page
        Given Rob goes to the 'user details' page for 'Admin'
        Then the user details page contains 'Admin's data

#    @default
#    Scenario: An user's details can be edited
#        Given Rob goes to 'user's user details page
#        When he edits the details: {fieldName: value}
#        Then the new details are saved

    @default
    Scenario: deleted users are deactivated
        Given Rob goes to the 'user management' page
        When he deletes 'user's account
        Then 'user's account has the status 'deactivated'

#    @extra_users
#    Scenario: BBMRI admins can page through lists of users
#        Given Rob goes to the 'user management' page
#        When he goes to the next list of users
#        Then the list of users has changed
