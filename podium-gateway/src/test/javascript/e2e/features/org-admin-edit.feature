Feature: organisation admins can manage organisations in the system (BRPREQ-38)

    @default
    Scenario: organisation admins can see their organisation
        Given VarnameBank_Admin goes to the 'organisation configuration' page
        When he sorts by 'Name'
        Then the overview contains the organisation's 'name, shortName' for the organisations 'VarnameBank'

    @default
    Scenario Outline: organisation admins can give organisation roles to users
        Given VarnameBank_Admin goes to the 'organisation edit' page for the organisation 'VarnameBank'
        When he adds user '<user>' with role '<role>'
        Then '<user>' has the role '<role>' in the organisation 'VarnameBank'

        Examples:
            | user                               | role                                                           |
            | blank user                         | Organisation administrator                                     |
            | blank user                         | Organisation coordinator                                       |
            | blank user                         | Reviewer                                                       |
            | blank user, blank user, blank user | Organisation administrator, Organisation coordinator, Reviewer |

    @default
    Scenario Outline: organisation admins can add requestTypes to organisations
        Given VarnameBank_Admin goes to the 'organisation edit' page for the organisation 'VarnameBank'
        When he indicates that '<types>' can be requested from 'VarnameBank'
        Then the organisation's data has changed

        Examples:
            | types                  |
            | Data                   |
            | Images                 |
            | Material               |
            | Data, Images, Material |

#    @default @org-users
#    Scenario Outline: organisation admins can remove organisation roles from users
#        Given VarnameBank_Admin goes to the 'organisation edit' page for the organisation 'VarnameBank'
#        When he removes user '<user>' with role '<role>'
#        Then '<user>' does not have the role '<role>' in the organisation 'VarnameBank'
#
#        Examples:
#            | user       | role                       |
#            | blank user | Organisation administrator |
#            | blank user | Organisation coordinator   |
#            | blank user | Reviewer                   |

