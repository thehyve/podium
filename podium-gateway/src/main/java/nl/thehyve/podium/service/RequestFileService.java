package nl.thehyve.podium.service;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
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
import java.util.UUID;

@Service
@Transactional
public class RequestFileService {

    private final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    FileSystem fileSystem = FileSystems.getDefault();

    @Autowired
    private RequestFileRepository requestFileRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestFileMapper requestFileMapper;

    /**
     * Create a new draft request.
     *
     * @param owner the user that owns the file. requestUUID for the request it is linked to and the MultipartFile
     * @return saved request representation
     */
    public RequestFileRepresentation addFile(IdentifiableUser owner, UUID requestUuid, MultipartFile file) {
        RequestFile requestFile = new RequestFile();
        requestFile.setOwner(owner.getUserUuid());
        Request request = requestRepository.findOneByUuid(requestUuid);
        requestFile.setRequest(request);

        try{
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
