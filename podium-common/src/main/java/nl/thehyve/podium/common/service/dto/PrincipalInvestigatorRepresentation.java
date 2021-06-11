/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;


import lombok.Data;
import nl.thehyve.podium.common.validation.Required;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * A DTO for the PrincipalInvestigator entity.
 */
@Data
public class PrincipalInvestigatorRepresentation implements Serializable {

    private Long id;

    @Required
    @Size(max = 150)
    private String name;

    @Email
    @Required
    @Size(max = 150)
    private String email;

    @Required
    @Size(max = 150)
    private String jobTitle;

    @Required
    @Size(max = 150)
    private String affiliation;

}
