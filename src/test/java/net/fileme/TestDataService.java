package net.fileme;

import net.fileme.domain.token.VerifyToken;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.service.EmailService;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import net.fileme.utils.RedisCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDataService {
    @Autowired
    private FileService fileService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private VerifyToken verifyToken;

    @Test
    void test(){
    }
}
