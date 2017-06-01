Feature: researcher can do requests (BRPREQ-134)

    @default
    @request
    Scenario Outline: list of organisations on the request form is filtered based on the type of request
        Given Linda goes to the 'new requests' page
        When she selects request types '<requestTypes>'
        Then the organisations '<organisations>' can be selected

        Examples:
            | requestTypes           | organisations        |
            | Data                   | DataBank, MultiBank  |
            | Images                 | ImageBank, MultiBank |
            | Material               | BioBank, MultiBank   |
            | Data, Images, Material | MultiBank            |

