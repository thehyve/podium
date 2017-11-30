package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.service.util.UserMapperHelper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class RequestFileMapper {

    @Autowired
    private UserMapperHelper userMapperHelper;

    public RequestFileRepresentation processingRequestFileDtoToRequestFile(RequestFile requestFile){
        RequestFileRepresentation requestFileRepresentation = new RequestFileRepresentation();
        requestFileRepresentation.setCreatedDate(requestFile.getCreatedDate());
        requestFileRepresentation.setLastModifiedDate(requestFile.getLastModifiedDate());
        requestFileRepresentation.setUuid(requestFile.getUuid());
        requestFileRepresentation.setRequest(requestFile.getRequest());

        requestFileRepresentation.setFileSize(requestFile.getFileByteSize());
        requestFileRepresentation.setFileName(requestFile.getFileName());
        requestFileRepresentation.setRequestFileType(requestFile.getRequestFileType());

        UserRepresentation owner = userMapperHelper.uuidToRemoteUserRepresentation(requestFile.getOwner());
        requestFileRepresentation.setOwner(owner);

        return requestFileRepresentation;
    }


}
