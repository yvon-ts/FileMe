package net.fileme.service;

import net.fileme.pojo.File;
import org.springframework.web.multipart.MultipartFile;

public interface ClientFileService {
    File createFile(MultipartFile clientFile);
    String upload(MultipartFile clientFile
            , Long userId
            , Long fileId
            , boolean toRemote);
}
