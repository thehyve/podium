package nl.thehyve.podium.internal;

import nl.thehyve.podium.common.resource.InternalRequestResource;
import nl.thehyve.podium.common.security.annotations.RequestUuidParameter;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URISyntaxException;
import java.util.UUID;

@Component
public class InternalRequestServer implements InternalRequestResource {

    @Autowired
    private RequestService requestService;

    @Override
    public ResponseEntity<RequestRepresentation> getRequest(@RequestUuidParameter @PathVariable("uuid") UUID uuid) throws URISyntaxException {
        RequestRepresentation request = requestService.findRequest(uuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }
}
