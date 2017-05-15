/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.common.service.dto;

import java.io.Serializable;

public class MessageRepresentation implements Serializable {
    String summary;
    String description;

    public String getSummary() { return summary; }

    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
