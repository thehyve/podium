Feature: Organization coordinators can manage requests for their organization (BRPREQ-23)

    @default
    @request
    Scenario: BRPREQ-23, As an organisation coordinator I want to see an overview of requests for my organization
        Given Databank_Coordinator goes to the 'organisation request overview' page
        When he sorts by 'Title'
        Then the overview contains the request's 'title, status, requestTypes, organisations, requesterName, piName' for the requests 'Request02'

#    @default
#    @request
#    Scenario: BRPREQ-27, As an organisation coordinator I want to send a request to review
#        Given Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
#        When he sends the request to review
#        Then the request is in Review with state 'Validation'
