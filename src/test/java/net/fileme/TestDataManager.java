package net.fileme;

import net.fileme.service.DataManagerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TestDataManager {
    @Autowired
    private DataManagerService dataManagerService;
    @Test
    void test(){
        List<Long> folders = new ArrayList<>();
        folders.add(1012L);
        folders.add(1048L);
        List<Long> files = new ArrayList<>();
        files.add(100L);
        files.add(200L);
        files.add(210L);
//        System.out.println("======relocateFolders======");
//        dataManagerService.relocateFolders(999L, folders);
//        System.out.println("======relocateFiles======");
//        dataManagerService.relocateFiles(999L, files);
//        dataManagerService.toTrashFolders(folders);
//        dataManagerService.recoverFolders(1L,folders);
//        dataManagerService.toTrashFiles(files);
        dataManagerService.recoverFiles(1L,files);


    }
}
