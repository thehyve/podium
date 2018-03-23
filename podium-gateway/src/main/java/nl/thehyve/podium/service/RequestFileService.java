package nl.thehyve.podium.service;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.common.enumeration.RequestFileType;
import nl.thehyve.podium.repository.RequestFileRepository;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.common.service.dto.RequestFileRepresentation;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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
    private PodiumProperties podiumProperties;

    @Autowired
    UserClientService userClientService;


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

    private static void checkAllowedToAddFile(AuthenticatedUser user, Request request) throws ActionNotAllowed {
        final Set<UUID> organisationUuids = request.getOrganisations();
        // Researchers can add files if it is in Draft or Revision status
        if (request.getRequester().equals(user.getUserUuid()) &&
            Status.isCurrentStatusAllowed(request.getOverviewStatus(), OverviewStatus.Draft, OverviewStatus.Revision)){
            return;
        }
        // Coordinators can add files in Validation or Review status
        if (user.getOrganisationAuthorities().entrySet().stream().anyMatch(entry ->
                organisationUuids.contains(entry.getKey()) && entry.getValue().contains(AuthorityConstants.ORGANISATION_COORDINATOR)) &&
                Status.isCurrentStatusAllowed(request.getOverviewStatus(), OverviewStatus.Validation, OverviewStatus.Review)){
            return;
        }
        throw ActionNotAllowed.forStatus(request.getOverviewStatus());
    }

    private Path getTempFile() throws IOException {
        String uploadDir = podiumProperties.getFiles().getUploadDir();
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            log.info("Create upload directory: {}", path);
            Files.createDirectories(path);
        }
        return Files.createTempFile(path,"", "");
    }

    /**
     * Create a new draft request.
     *
     * @param user the user that adds the file.
     * @param requestUuid the uuid of the request the file is added to.
     * @param file the file to add.
     * @param requestFileType the file type.
     * @return saved request representation
     */
    public RequestFileRepresentation addFile(AuthenticatedUser user, UUID requestUuid, MultipartFile file,
                                             RequestFileType requestFileType) throws ActionNotAllowed, AccessDenied, IOException {

        Request request = findRequest(requestUuid);
        checkAllowedToAddFile(user, request);

        RequestFile requestFile = new RequestFile();
        requestFile.setOwner(user.getUserUuid());
        requestFile.setRequest(request);
        requestFile.setRequestFileType(requestFileType);
        requestFile.setFileName(file.getOriginalFilename());
        requestFile.setFileByteSize(file.getSize());

        Path tempFile = getTempFile();
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        requestFile.setFileLocation(tempFile.getFileName().toString());

        requestFile = requestFileRepository.save(requestFile);

        return requestFileMapper.requestFileToRequestFileDto(requestFile);
    }

    public RequestFile copyFile(RequestFile source) throws IOException {
        RequestFile target = new RequestFile();
        requestFileMapper.minimalRequestFileToRequestFile(source, target);
        Path targetFile = getTempFile();
        Path sourceFile = new File(source.getFileLocation()).toPath();
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        target.setFileLocation(targetFile.getFileName().toString());
        return target;
    }

    @Transactional(readOnly = true)
    public InputStreamResource getFileResource(UUID requestUuid, UUID fileUuid) throws IOException{
        RequestFile requestFile = findRequestFile(requestUuid, fileUuid);
        String uploadDir = podiumProperties.getFiles().getUploadDir();

        Path path = Paths.get(uploadDir +'/'+ requestFile.getFileLocation());
        InputStream input = new FileInputStream(path.toFile());
        return new InputStreamResource(input, path.getFileName().toString());
    }

    @Transactional(readOnly = true)
    public List<RequestFileRepresentation> getFilesForRequest(UUID requestUuid){
        Request request = findRequest(requestUuid);
        List<RequestFile> files = requestFileRepository.findDistinctByRequestAndDeletedFalse(request);

        List<RequestFileRepresentation> representations = new ArrayList<>();
        for(RequestFile file : files){
            RequestFileRepresentation representation = requestFileMapper.requestFileToRequestFileDto(file);
            representations.add(representation);
        }

        return representations;
    }

    public void deleteFile(IdentifiableUser requester, UUID requestUuid, UUID fileUuid) throws ResourceNotFound, IOException {
        RequestFile requestFile = findRequestFile(requestUuid, fileUuid);

        if (!requestFile.getOwner().equals(requester.getUserUuid())) {
            // Only owners can delete files.
            throw new AccessDenied("Only owners can delete files.");
        }

        requestFile.setDeleted(true);
        requestFileRepository.save(requestFile);
        String uploadDir = podiumProperties.getFiles().getUploadDir();
        String fileLocation = requestFile.getFileLocation();
        Files.delete(Paths.get(uploadDir + '/' + fileLocation));
    }

    public RequestFileRepresentation setFileType(UUID requestUuid, UUID fileUuid, RequestFileType filetype) {
        RequestFile requestFile = findRequestFile(requestUuid, fileUuid);
        requestFile.setRequestFileType(filetype);
        requestFileRepository.save(requestFile);
        return requestFileMapper.requestFileToRequestFileDto(requestFile);
    }

    @Transactional(readOnly = true)
    public boolean hasUnsetFileType(Request request) {
        List<RequestFile> files = requestFileRepository.findDistinctByRequestAndDeletedFalse(request);
        return files.stream().anyMatch(file -> file.getRequestFileType() == RequestFileType.NONE);
    }

}
