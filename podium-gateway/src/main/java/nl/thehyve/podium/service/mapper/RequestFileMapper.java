package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class RequestFileMapper {

    public RequestFileRepresentation processingRequestFileDtoToRequestFile(RequestFile requestFile){
        RequestFileRepresentation requestFileRepresentation = new RequestFileRepresentation();
        requestFileRepresentation.setCreatedDate(requestFile.getCreatedDate());
        requestFileRepresentation.setId(requestFile.getId());
        requestFileRepresentation.setLastModifiedDate(requestFile.getLastModifiedDate());
        requestFileRepresentation.setOwner(requestFile.getOwner());
        requestFileRepresentation.setUuid(requestFile.getUuid());

        return requestFileRepresentation;
    }
}
