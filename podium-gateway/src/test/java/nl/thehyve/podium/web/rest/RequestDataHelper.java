package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.service.dto.PrincipalInvestigatorRepresentation;
import nl.thehyve.podium.common.service.dto.RequestDetailRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import org.mockito.internal.util.collections.Sets;

public class RequestDataHelper {

    static void setRequestData(RequestRepresentation request) {
        RequestDetailRepresentation details = request.getRequestDetail();
        details.setTitle("Test title");
        details.setBackground("Background of the request");
        details.setResearchQuestion("Does it work?");
        details.setHypothesis("H0");
        details.setMethods("Testing");
        details.setSearchQuery("q");
        details.setRequestType(Sets.newSet(RequestType.Data, RequestType.Material));
        PrincipalInvestigatorRepresentation principalInvestigator = details.getPrincipalInvestigator();
        principalInvestigator.setName("Test Person");
        principalInvestigator.setEmail("pi@local");
        principalInvestigator.setJobTitle("Tester");
        principalInvestigator.setAffiliation("The Organisation");
    }

}
