package net.fileme.service;

import net.fileme.domain.pojo.File;
import org.springframework.web.multipart.MultipartFile;

public interface RemoteDataService {
    void upload(MultipartFile part, File file);

    byte[] download(String fileName);

    void delete(String fileName);
}
