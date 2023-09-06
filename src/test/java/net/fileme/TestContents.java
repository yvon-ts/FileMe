package net.fileme;

import net.fileme.service.impl.ClientFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestContents {

    @Autowired
    private ClientFileServiceImpl clientFileService;
    @Test
    void testRelocate(){
//        System.out.println("======relocateFolders======");
        List<Long> folders = new ArrayList<>();
        folders.add(1042L);
        folders.add(1048L);
        folders.add(999L);
//        clientFileService.relocateFolders(1L, 0L, folders);
//        System.out.println("======relocateFiles======");
        List<Long> files = new ArrayList<>();
        files.add(100L);
        files.add(200L);
        files.add(210L);
//        clientFileService.relocateFiles(1L, 0L, files);
        System.out.println("======relocateBatch======");
        Map<String, List<Long>> data = new HashMap<>();
        data.put("folderIds", folders);
        data.put("fileIds", files);
        clientFileService.relocateBatch(1L, 999L, data);

    }
}
