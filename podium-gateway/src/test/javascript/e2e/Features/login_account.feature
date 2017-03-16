Feature: All users have to login. (BRPREQ-2, BRPREQ-4)

    Scenario Outline: every role is able to login
        Given <user> goes to the 'sign in' page
        When he attempts to login
        Then he is on the 'Dashboard' page

        Examples:
            | user     |
            | Rob      |
            | Simone   |
#            | Brigitte |

#    Scenario: failing to login locks the account
#        Given <user> goes to the 'sign in' page
#        When 'he' attempts to login incorrectly '5' times
#        And 'he' attempts to login
#        Then 'he' is locked out

    Scenario: users can edit their account profile
        Given Simone goes to the 'profile' page
        When she edits the details:
        """
            {
               "firstName":"newFirstName",
               "lastName":"newLastName"
            }
        """
        Then the new details are saved

    Scenario: some fields in a user's profile cannot be edited
        Given Simone goes to the 'profile' page
        Then the following fields are not editable:
        """
            {
               "institute":"The Hyve"
            }
        """

#    Scenario: register a new user
#        Given 'Simone' is on the 'registration' page
#        When 'she' registersfor a new account
#        Then an email is sent
#
#    Scenario: all fields are mandatory to register a new user
#        Given 'Simone' is on the 'registration' page
#        When 'she' forgets to fill a field in the registration form
#        Then 'she' is not registered
