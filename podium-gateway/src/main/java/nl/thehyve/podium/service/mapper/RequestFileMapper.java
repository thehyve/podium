package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.common.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.service.util.UserMapperHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {
        RequestMapper.class
})
public abstract class RequestFileMapper {

    @Autowired
    private UserMapperHelper userMapperHelper;

    @Autowired
    private RequestMapper requestMapper;

    @Mappings({
            @Mapping(target = "request", ignore = true),
            @Mapping(target = "owner", ignore = true),
    })
    abstract RequestFileRepresentation minimalRequestFileToRequestFileDto(RequestFile requestFile);

    public RequestFileRepresentation requestFileToRequestFileDto(RequestFile requestFile){
        RequestFileRepresentation requestFileRepresentation = minimalRequestFileToRequestFileDto(requestFile);
        requestFileRepresentation.setRequest(requestMapper.minimalRequestToRequestDTO(requestFile.getRequest()));
        requestFileRepresentation.setOwner(userMapperHelper.uuidToRemoteUserRepresentation(requestFile.getOwner()));
        return requestFileRepresentation;
    }

    /**
     * Copy properties of the request file representation to the entity,
     * except id, uuid, request, owner and file location.
     * @param source data to be mapped
     * @param target entity to map to
     * @return the mapping target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "uuid", ignore = true),
            @Mapping(target = "request", ignore = true),
            @Mapping(target = "fileLocation", ignore = true)
    })
    public abstract RequestFile minimalRequestFileToRequestFile(
            RequestFile source,
            @MappingTarget RequestFile target);

}
