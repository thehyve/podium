/**
 * Copyright (c) 2017 The Hyve B.V.
 * This code is licensed under the GNU General Public License,
 * version 3, or (at your option) any later version.
 */

package nl.thehyve.podium.common.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Representation class for created request draft from external call.
 */
@Data
public class DraftRepresentation implements Serializable {
    private RequestRepresentation draft;
    private List<Map<String, String>> missingOrganisations = new ArrayList<>();
}
