package nl.thehyve.podium.service;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.domain.RequestFile;
import nl.thehyve.podium.repository.RequestFileRepository;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.service.mapper.RequestFileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

        try{
            String uploadFolder = "/tmp/podium_data/" + System.currentTimeMillis() + "/";
            byte[] bytes = file.getBytes();
            String pathString = uploadFolder + file.getOriginalFilename();
            Path path = Paths.get(pathString);
            File posFile = new File(path.toString());
            File neededDir = new File(uploadFolder);

            // Add the required folder if it doesn't exist yet
            if(!neededDir.exists()){
                neededDir.mkdir();
            }
            // Doublecheck if this doesn't exist, otherwise call this function again to generate a new uploadFolder string.
            if(!posFile.exists()){
                Files.write(path, bytes);
                requestFile.setFileLocation(path.toString());
            } else {
                return this.addFile(owner, requestUUID, file);
            }
        } catch (IOException e) {
            log.error("Exception saving File", e);
        }

        requestFileRepository.save(requestFile);

        return requestFileMapper.processingRequestFileDtoToRequestFile(requestFile);
    }
}
