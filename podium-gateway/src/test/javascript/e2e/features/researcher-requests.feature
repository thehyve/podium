Feature: researcher can do requests (BRPREQ-134, BRPREQ-146, BRPREQ-58)

    @default
    @request
    Scenario Outline: BRPREQ-134, list of organisations on the request form is filtered based on the type of request
        Given Linda goes to the 'new requests' page
        When she selects request types '<requestTypes>'
        Then the organisations '<organisations>' can be selected

        Examples:
            | requestTypes           | organisations                           |
#            | Data                   | DataBank, MultiBank                     |
#            | Images                 | ImageBank, MultiBank                    |
#            | Material               | BioBank, MultiBank                      |
            | Data, Images, Material | DataBank, ImageBank, BioBank, MultiBank |

    @default
    @request
    Scenario: BRPREQ-146, as a researcher I want to see the request details
        Given Linda goes to the 'request details' page for the request 'Request01' submitted to 'MultiBank'
        Then the request details for 'Request01' submitted to 'MultiBank' are shown

    @default
    @request
    Scenario: BRPREQ-3, As a researcher I want to see an overview of my requests
        Given Linda goes to the 'request overview' page
        When she sorts by 'Title'
        Then the overview contains the request's 'title, status, requestTypes, organisations' for the requests 'Request01-a, Request01-b, Request02' Request01-b, Request01-a, Request02

    @default
    @request
    Scenario: BRPREQ-58, As a researcher I want to edit and resubmit my request where revision is required
        Given 'Request02' needs revision
        And Linda goes to the 'revise requests' page for the request 'Request02' submitted to 'DataBank'
        When Linda revises and 'submits' the request
        Then the request is in the 'Review' state

#    @default
#    @request
#    Scenario: BRPREQ-58, As a researcher I want to save intermediate changes revision is required
#        Given 'Request02' needs revision
#        When Linda revises and saves 'Request02'
#        Then the changes are saved
