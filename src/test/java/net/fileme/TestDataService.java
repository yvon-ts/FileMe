package net.fileme;

import net.fileme.domain.mapper.FolderMapper;
import net.fileme.service.FileService;
import net.fileme.service.FolderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TestDataService {
    @Autowired
    private FileService fileService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private FolderMapper folderMapper;

    @Test
    void testRecursive(){
        List<Object> superFolders = folderMapper.getSuperFolders(1697549876074434562L);
        System.out.println(superFolders);
    }

    @Test
    void test(){
        List<Long> list = new ArrayList<>();
        list.add(200L);
        fileService.hardDelete(list);
    }
}
