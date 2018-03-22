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
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class RequestFileService {

    private final Logger log = LoggerFactory.getLogger(RequestFileService.class);

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


    private Request findRequest(UUID requestUuid) {
        Request request = requestRepository.findOneByUuid(requestUuid);
        if (request == null) {
            throw new ResourceNotFound("Request not found.");
        }
        return request;
    }

    private RequestFile findRequestFile(UUID requestUuid, UUID fileUuid) {
        Request request = findRequest(requestUuid);
        RequestFile requestFile = requestFileRepository.findOneByRequestAndUuidAndDeletedFalse(request, fileUuid);
        if (requestFile == null) {
            throw new ResourceNotFound("File not found.");
        }
        return requestFile;
    }

    /**
     * Create a new draft request.
     *
     * @param owner the user that owns the file. requestUUID for the request it is linked to and the MultipartFile
     * @return saved request representation
     */
    @Transactional
    public RequestFileRepresentation addFile(IdentifiableUser owner, UUID requestUuid, MultipartFile file,
                                             RequestFileType requestFileType) throws ActionNotAllowed, AccessDenied, IOException {

        Request request = findRequest(requestUuid);

        boolean allowedToAdd = false;

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

        // Coordinator can add files in Review status
        if(securityService.isCurrentUserInAnyOrganisationRole(organisationUuids, AuthorityConstants.ORGANISATION_COORDINATOR)){
            if(overviewStatus == OverviewStatus.Review){
                allowedToAdd = true;
            }
        }

        if (!allowedToAdd){
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

    @Transactional(readOnly = true)
    public InputStreamResource getFileResource(UUID requestUuid, UUID fileUuid) throws IOException{
        RequestFile requestFile = findRequestFile(requestUuid, fileUuid);
        String uploadDir = podiumProperties.getFiles().getUploadDir();

        Path path = Paths.get(uploadDir +'/'+ requestFile.getFileLocation());
        InputStream input = new FileInputStream(path.toFile());
        return new InputStreamResource(input);
    }

    @Transactional(readOnly = true)
    public List<RequestFileRepresentation> getFilesForRequest(UUID requestUuid){
        Request request = findRequest(requestUuid);
        List<RequestFile> files = requestFileRepository.findDistinctByRequestAndDeletedFalse(request);

        List<RequestFileRepresentation> representations = new ArrayList<>();
        for(RequestFile file : files){
            RequestFileRepresentation representation = requestFileMapper.processingRequestFileToRequestFileDto(file);
            representations.add(representation);
        }

        return representations;
    }

    @Transactional
    public void deleteFile(IdentifiableUser requester, UUID requestUuid, UUID fileUuid) throws ResourceNotFound, IOException {
        RequestFile requestFile = findRequestFile(requestUuid, fileUuid);

        if (!requestFile.getOwner().equals(requester.getUserUuid())) {
            // Only owners can delete files.
            throw new ResourceNotFound("File not found");
        }
        requestFile.setDeleted(true);
        requestFileRepository.save(requestFile);
        String uploadDir = podiumProperties.getFiles().getUploadDir();
        String fileLocation = requestFile.getFileLocation();
        Files.delete(Paths.get(uploadDir + '/' + fileLocation));
    }

    @Transactional
    public RequestFileRepresentation setFileType(UUID requestUuid, UUID fileUuid, RequestFileType filetype) {
        RequestFile requestFile = findRequestFile(requestUuid, fileUuid);
        requestFile.setRequestFileType(filetype);
        requestFileRepository.save(requestFile);
        return requestFileMapper.processingRequestFileToRequestFileDto(requestFile);
    }

    @Transactional(readOnly = true)
    public Boolean hasUnsetFile(Request request) {
        List<RequestFile> files = requestFileRepository.findDistinctByRequestAndDeletedFalse(request);
        return files.stream().anyMatch(file -> file.getRequestFileType() == RequestFileType.NONE);
    }

}
