package net.fileme;

import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDataService {
    @Autowired
    private FileService fileService;
    @Autowired
    private FolderService folderService;

    @Test
    void test(){
        System.out.println(fileService.getTrashIds(2L));
    }
}
