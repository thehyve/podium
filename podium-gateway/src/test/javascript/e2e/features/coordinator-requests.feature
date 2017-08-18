Feature: Organization coordinators can manage requests for their organization (BRPREQ-23, BRPREQ-158, BRPREQ-56)

    @default
    @request
    Scenario: BRPREQ-23, As an organisation coordinator I want to see an overview of requests for my organization
        Given Databank_Coordinator goes to the 'organisation request overview' page
        When he sorts by 'Title'
        Then the overview contains the request's 'title, status, requestTypes, organisations, requesterName, name' for the requests 'Request02'

    @default
    @request
    Scenario: BRPREQ-27, As an organisation coordinator I want to send a request to review
        Given Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he sends the request for review
        Then the request has the status 'Review'

    @default
    @request
    Scenario: BRPREQ-158, As an organisation coordinator I want to reject a request
        Given Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he rejects the request
        Then the request has the status 'Rejected'

    @default
    @request
    Scenario: BRPREQ-158, As an organisation coordinator I want to send a request back for revision
        Given Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he sends the request back for revision
        Then the request has the status 'Revision'

    @default
    @request
    Scenario: BRPREQ-158 As an organisation coordinator I want to have a request details page
        Given Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        Then the request cannot be edited

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to reject a request that is in Review
        Given 'Request02' needs review
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he rejects the request
        Then the request has the status 'Rejected'

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to send a request back for revision that is in Review
        Given 'Request02' needs review
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he sends the request back for revision
        Then the request has the status 'Revision'

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to Approve a request that is in Review
        Given 'Request02' needs review
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he approves the request
        Then the request has the status 'Approved'

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to start delivery on a request that is approved
        Given 'Request02' is approved
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he starts delivery on the request
        Then the request has the status 'Delivery'
        And there are the following deliveries:
        """
            [{ "type": "Data", "status": "Preparation" }]
        """

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to release the materials related to a delivery
        Given 'Request02's delivery has started
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he releases delivery 'Data'
        Then there are the following deliveries:
        """
            [{ "type": "Data", "status": "Released" }]
        """

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to mark a delivery for which the materials have been recieved
        Given 'Request02's delivery has started
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he releases delivery 'Data'
        And he marks released delivery 'Data' as received
        Then there are the following deliveries:
        """
            [{ "type": "Data", "status": "Closed" }]
        """

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to mark a delivery for which the materials have been recieved
        Given 'Request02's delivery has started
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he releases delivery 'Data'
        And he marks released delivery 'Data' as received
        And he finalises the request
        Then the request has the status 'Delivered'

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to cancel a delivery
        Given 'Request02's delivery has started
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he cancels delivery 'Data'
        Then there are the following deliveries:
        """
            [{ "type": "Data", "status": "Closed" }]
        """

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to finalize the request after canceling all deliveries
        Given 'Request02's delivery has started
        And Databank_Coordinator goes to the 'request details' page for the request 'Request02' submitted to 'DataBank'
        When he cancels delivery 'Data'
        And he finalises the request
        Then the request has the status 'Cancelled'

    @default
    @request
    Scenario: BRPREQ-56, As an organisation coordinator I want to finalize the request after canceling some deliveries
        Given 'Request03's delivery has started
        And Request_Coordinator goes to the 'request details' page for the request 'Request03' submitted to 'MultiBank'
        When he releases delivery 'Data, Material'
        And he marks released delivery 'Data' as received
        And he cancels delivery 'Material, Images'
        And he finalises the request
        Then the request has the status 'Partially_Delivered'
