package nl.thehyve.podium.service;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.repository.RequestFileRepository;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.service.mapper.RequestFileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class RequestFileService {

    private final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    @Autowired
    private RequestFileRepository requestFileRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestFileMapper requestFileMapper;

    @Autowired
    private SecurityService securityService;

    /**
     * Create a new draft request.
     *
     * @param owner the user that owns the file. requestUUID for the request it is linked to and the MultipartFile
     * @return saved request representation
     */
    public RequestFileRepresentation addFile(IdentifiableUser owner, UUID requestUuid, MultipartFile file) throws
        ActionNotAllowed {
        RequestFile requestFile = new RequestFile();
        requestFile.setOwner(owner.getUserUuid());
        Request request = requestRepository.findOneByUuid(requestUuid);
        requestFile.setRequest(request);
        Set<UUID> organisationUuids = request.getOrganisations();
        RequestStatus requestStatus = request.getStatus();

        Boolean allowedToAdd = false;

        // Researchers can add files if it is in Draft(/Revision) status
        if(securityService.isCurrentUserInAnyOrganisationRole(organisationUuids, AuthorityConstants.RESEARCHER)){
            if(request.getStatus() == RequestStatus.Draft){
                allowedToAdd = true;
            }
        }
        // Coordinator can add files in Validation status
        if(securityService.isCurrentUserInAnyOrganisationRole(organisationUuids, AuthorityConstants.ORGANISATION_COORDINATOR)){
            if(request.getStatus() == RequestStatus.Review){
                allowedToAdd = true;
            }
        }

        // Reviewer can add files in Review status
        if(securityService.isCurrentUserInAnyOrganisationRole(organisationUuids, AuthorityConstants.REVIEWER)){
            if(request.getStatus() == RequestStatus.Review){
                allowedToAdd = true;
            }
        }

        if(!allowedToAdd){
            throw ActionNotAllowed.forStatus(request.getStatus());
        }
        try{
            //TODO: Make this folder configurable
            String uploadFolder = "/tmp/podium_data/" + System.currentTimeMillis() + "/";
            byte[] bytes = file.getBytes();
            String pathString = uploadFolder + file.getOriginalFilename();
            Path path = Paths.get(pathString);
            File posFile = new File(path.toString());
            File neededDir = new File(uploadFolder);

            // Add the required folder(s) if it doesn't exist yet
            if(!neededDir.exists()){
                neededDir.mkdirs();
            }
            // Doublecheck if this doesn't exist, otherwise call this function again to generate a new uploadFolder string.
            if(!posFile.exists()){
                Files.write(path, bytes);
                requestFile.setFileLocation(path.toString());
            } else {
                return this.addFile(owner, requestUuid, file);
            }
        } catch (IOException e) {
            log.error("Exception saving File", e);
        }

        requestFileRepository.save(requestFile);

        return requestFileMapper.processingRequestFileDtoToRequestFile(requestFile);
    }

    public ByteArrayResource getFile(IdentifiableUser requester, UUID requestUUID, UUID fileUuid) throws IOException{
        RequestFile requestFile = requestFileRepository.findOneByUuidAndDeletedFalse(fileUuid);

        Path path = Paths.get(requestFile.getFileLocation());
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    public List<RequestFileRepresentation> getFilesForRequest(IdentifiableUser requester, UUID requestUUID){
        Request request = requestRepository.findOneByUuid(requestUUID);

        List<RequestFile> files = requestFileRepository.findDistinctByRequestAndDeletedFalse(request);

        List<RequestFileRepresentation> representations = new ArrayList<RequestFileRepresentation>();
        for(RequestFile file : files){
            RequestFileRepresentation representation = requestFileMapper.processingRequestFileDtoToRequestFile(file);
            representations.add(representation);
        }

        return representations;
    }

    public Boolean deleteFile(IdentifiableUser requester, UUID fileUuid){
        RequestFile requestFile = requestFileRepository.findOneByUuidAndDeletedFalse(fileUuid);

        //Only owners can delete files.
        if(requestFile.getOwner().equals(requester.getUserUuid())){
            requestFile.setDeleted(true);
            requestFileRepository.save(requestFile);
            return true;
        } else {
            return false;
        }
    }
}
