package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class RequestFileMapper {

    public RequestFileRepresentation processingRequestFileDtoToRequestFile(RequestFile requestFile){
        RequestFileRepresentation requestFileRepresentation = new RequestFileRepresentation();

        return requestFileRepresentation;
    }
}
