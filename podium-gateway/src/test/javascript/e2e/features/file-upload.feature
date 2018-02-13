Feature: files can be uploaded and downloaded from requests.

    @default
    @request
    Scenario: Linda can add a file to a draft
        Given Linda goes to the 'new requests' page
        When she fills the new draft with data from 'DraftWithFile'
        Then the draft is saved
        And the draft has the files 'example' attached

    @default
    @request
    Scenario: Linda can remove a file from a draft
        Given Linda goes to the 'new requests' page
        When she fills the new draft with data from 'DraftWithFile'
        When the file is removed
        Then the draft is saved
        And the draft has the files '' attached

