package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.service.util.UserMapperHelper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {
        RequestMapper.class
})
public abstract class RequestFileMapper {

    @Autowired
    private UserMapperHelper userMapperHelper;

    @Autowired
    private RequestMapper requestMapper;

    public RequestFileRepresentation processingRequestFileToRequestFileDto(RequestFile requestFile){
        RequestFileRepresentation requestFileRepresentation = new RequestFileRepresentation();
        requestFileRepresentation.setCreatedDate(requestFile.getCreatedDate());
        requestFileRepresentation.setLastModifiedDate(requestFile.getLastModifiedDate());
        requestFileRepresentation.setUuid(requestFile.getUuid());
        requestFileRepresentation.setRequest(requestMapper.minimalRequestToRequestDTO(requestFile.getRequest()));

        requestFileRepresentation.setFileSize(requestFile.getFileByteSize());
        requestFileRepresentation.setFileName(requestFile.getFileName());
        requestFileRepresentation.setRequestFileType(requestFile.getRequestFileType());

        UserRepresentation owner = userMapperHelper.uuidToRemoteUserRepresentation(requestFile.getOwner());
        requestFileRepresentation.setOwner(owner);

        return requestFileRepresentation;
    }

}
