package nl.thehyve.podium.service;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.enumeration.RequestFileType;
import nl.thehyve.podium.repository.RequestFileRepository;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.service.mapper.RequestFileMapper;
import nl.thehyve.podium.service.mapper.RequestMapper;
import nl.thehyve.podium.service.util.UserMapperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private PodiumProperties podiumProperties;

    @Autowired
    private UserMapperHelper userMapperHelper;

    @Autowired
    private RequestMapper requestMapper;

    /**
     * Create a new draft request.
     *
     * @param owner the user that owns the file. requestUUID for the request it is linked to and the MultipartFile
     * @return saved request representation
     */
    public RequestFileRepresentation addFile(IdentifiableUser owner, UUID requestUuid, MultipartFile file,
                                             RequestFileType requestFileType) throws ActionNotAllowed, AccessDenied, IOException {

        Request request = requestRepository.findOneByUuid(requestUuid);

        Boolean allowedToAdd = false;

        Set<UUID> organisationUuids = request.getOrganisations();
        UUID requester = request.getRequester();
        UserRepresentation ownerRepresentation = userMapperHelper.uuidToRemoteUserRepresentation(owner.getUserUuid());
        Set<String> authorities = ownerRepresentation.getAuthorities();

        RequestRepresentation requestRepresentation = requestMapper.overviewRequestToRequestDTO(request);
        OverviewStatus overviewStatus = requestRepresentation.getStatus();

        // Researchers can add files if it is in Draft(/Revision) status
        if(authorities.contains(AuthorityConstants.RESEARCHER) &&
            (overviewStatus == OverviewStatus.Draft || overviewStatus == OverviewStatus.Revision)&&
            requester.equals(owner.getUserUuid())){
            allowedToAdd = true;
        }
        // Coordinator can add files in Validation status
        if(securityService.isCurrentUserInAnyOrganisationRole(organisationUuids, AuthorityConstants.ORGANISATION_COORDINATOR)){
            if(overviewStatus == OverviewStatus.Validation){
                allowedToAdd = true;
            }
        }

        // Reviewer can add files in Review status
        if(securityService.isCurrentUserInAnyOrganisationRole(organisationUuids, AuthorityConstants.REVIEWER)){
            if(overviewStatus == OverviewStatus.Review){
                allowedToAdd = true;
            }
        }

        if(!allowedToAdd){
            throw ActionNotAllowed.forStatus(request.getStatus());
        }

        RequestFile requestFile = new RequestFile();
        requestFile.setOwner(owner.getUserUuid());

        requestFile.setRequest(request);

        requestFile.setRequestFileType(requestFileType);

        String uploadDir = podiumProperties.getFiles().getUploadDir();
        requestFile.setFileName(file.getOriginalFilename());
        requestFile.setFileByteSize(file.getSize());
        Path path = Paths.get(uploadDir);

        Path tempFile = Files.createTempFile(path,"", "");
        Files.write(tempFile, file.getBytes());
        requestFile.setFileLocation(tempFile.getFileName().toString());

        requestFileRepository.save(requestFile);

        return requestFileMapper.processingRequestFileToRequestFileDto(requestFile);
    }

    public ByteArrayResource getFile(IdentifiableUser requester, UUID fileUuid) throws IOException{
        RequestFile requestFile = requestFileRepository.findOneByUuidAndDeletedFalse(fileUuid);
        String uploadDir = podiumProperties.getFiles().getUploadDir();

        Path path = Paths.get(uploadDir +'/'+ requestFile.getFileLocation());
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    public List<RequestFileRepresentation> getFilesForRequest(IdentifiableUser requester, UUID requestUUID){
        Request request = requestRepository.findOneByUuid(requestUUID);

        List<RequestFile> files = requestFileRepository.findDistinctByRequestAndDeletedFalse(request);

        List<RequestFileRepresentation> representations = new ArrayList<>();
        for(RequestFile file : files){
            RequestFileRepresentation representation = requestFileMapper.processingRequestFileToRequestFileDto(file);
            representations.add(representation);
        }

        return representations;
    }

    public void deleteFile(IdentifiableUser requester, UUID fileUuid) throws ResourceNotFound, IOException {
        RequestFile requestFile = requestFileRepository.findOneByUuidAndDeletedFalse(fileUuid);

        //Only owners can delete files.
        if(requestFile.getOwner().equals(requester.getUserUuid())){
            requestFile.setDeleted(true);
            requestFileRepository.save(requestFile);
            String uploadDir = podiumProperties.getFiles().getUploadDir();
            String fileLocation = requestFile.getFileLocation();
            Files.delete(Paths.get(uploadDir + '/' + fileLocation));
        } else {
            throw new ResourceNotFound("File not found");
        }
    }

    public RequestFileRepresentation setFileType(IdentifiableUser requester, UUID fileUuid, RequestFileType filetype)
        throws ActionNotAllowed{

        RequestFile requestFile = requestFileRepository.findOneByUuidAndDeletedFalse(fileUuid);
        if(requester.getUserUuid().equals(requestFile.getOwner())){
            requestFile.setRequestFileType(filetype);
            requestFileRepository.save(requestFile);
            return requestFileMapper.processingRequestFileToRequestFileDto(requestFile);
        }else {
            throw new ActionNotAllowed("Only Owner can set type");
        }


    }
}
