package net.fileme.service;

import net.fileme.domain.pojo.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RemoteDataService {
    void uploadRemote(MultipartFile part, File file);

    byte[] getRemoteByteArray(String fileName);

    void handleRemoteDelete(Long userId, List<Long> fileIds);
}
