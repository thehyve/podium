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

    @default
    Scenario: BBMRI_Admin can see user details
        Given BBMRI_Admin goes to the user details page for 'VarnameBank_Admin'
        Then the user details page contains 'VarnameBank_Admin's data

    @default
    Scenario: BBMRI_Admin can edit user details
        Given BBMRI_Admin goes to the user details page for 'VarnameBank_Admin'
        When he edits the details 'login, firstName, lastName, email'
        Then the new details are saved

    @default
    Scenario: BBMRI_Admin can delete users
        Given BBMRI_Admin goes to the 'user management' page
        When he deletes 'Linda's account
        Then her account is removed
