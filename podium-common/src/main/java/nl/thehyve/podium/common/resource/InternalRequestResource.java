package nl.thehyve.podium.common.resource;

import nl.thehyve.podium.common.security.annotations.RequestUuidParameter;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Resource for fetching requests.
 *
 * The implementing class should inject itself into the {@link SecurityService} in order
 * for that server to be able to check access policy rules based on request properties.
 */
@RequestMapping("/internal")
public interface InternalRequestResource {

    /**
     * GET  /requests/:uuid : get the request with the specified uuid.
     *
     * @param uuid the uuid of the request to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the request, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/request/uuid/{uuid}", method = RequestMethod.GET)
    ResponseEntity<RequestRepresentation> getRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid) throws URISyntaxException;

}
