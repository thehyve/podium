Feature: For a BBMRI admin to do his or her work they need information from the system (BRPREQ-32).

    @default
    Scenario: BBMRI admins can see users in the system
        Given BBMRI_Admin goes to the 'user management' page
        When he sorts by 'Login'
        Then the overview contains the user's 'login, email, emailVerified, adminVerified' for the users 'Admin, BBMRI_Admin, blank user, Dave, Linda, VarnameBank_Admin'

    @default
    Scenario Outline: user management page is sortable by <sortBy>
        Given BBMRI_Admin goes to the 'user management' page
        When he sorts by '<sortBy>'
        Then users are displayed in the following order: '<userOrder>'

        Examples:
            | sortBy | userOrder                                                      |
            | Login  | Admin, BBMRI_Admin, blank user, Dave, Linda, VarnameBank_Admin |
            | Email  | Admin, BBMRI_Admin, blank user, Linda, Dave, VarnameBank_Admin |

#    @default
#    Scenario: An user has a details page
#        Given Rob goes to the 'user details' page for  the user 'Admin'
#        Then the user details page contains 'Admin's data

#    @default
#    Scenario: An user's details can be edited
#        Given Rob goes to 'user's user details page
#        When he edits the details: {fieldName: value}
#        Then the new details are saved

#    @default
#    Scenario: deleted users are deactivated
#        Given Rob goes to the 'user management' page
#        When he deletes 'user's account
#        Then 'user's account has the status 'deactivated'

#    @extra_users
#    Scenario: BBMRI admins can page through lists of users
#        Given Rob goes to the 'user management' page
#        When he goes to the next list of users
#        Then the list of users has changed
