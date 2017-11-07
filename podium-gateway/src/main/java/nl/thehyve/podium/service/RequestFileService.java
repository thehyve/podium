package nl.thehyve.podium.service;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.repository.RequestFileRepository;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.service.mapper.RequestFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class RequestFileService {
    @Autowired
    private RequestFileRepository requestFileRepository;

    @Autowired
    private RequestFileMapper requestFileMapper;

    /**
     * Create a new draft request.
     *
     * @param owner the user that owns the file. requestUUID for the request it is linked to and the MultipartFile
     * @return saved request representation
     */
    public RequestFileRepresentation addFile(IdentifiableUser owner, UUID requestUUID, MultipartFile file) {
        RequestFile requestFile = new RequestFile();
        requestFile.setOwner(owner.getUserUuid());
        requestFile.setRequest(requestUUID);
        requestFileRepository.save(requestFile);

        return requestFileMapper.processingRequestFileDtoToRequestFile(requestFile);
    }


}
