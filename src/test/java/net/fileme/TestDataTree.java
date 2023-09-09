package net.fileme;

import net.fileme.service.DataTreeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TestDataTree {
    @Autowired
    private DataTreeService dataTreeService;
    @Test
    void testDataTreeService(){
//        System.out.println("======findSubFolderIds======");
//        System.out.println(dataTreeService.findSubFolderIds(1L, 1036L));
//        System.out.println("======findSubFileIds======");
//        System.out.println(dataTreeService.findSubFileIds(1L, 1035L));
//        System.out.println("======findSubIds======");
//        System.out.println(dataTreeService.findSubIds(1L, 1028L));
//        System.out.println("======findSubFolders======");
//        System.out.println(dataTreeService.findSubFolders(1L, 1036L));
//        System.out.println("======findSubFiles======");
//        System.out.println(dataTreeService.findSubFiles(1L, 1035L));
//        System.out.println("======findTreeIds======");
//        System.out.println(dataTreeService.findTreeIds(1L, 1012L));
        List<Long> list = new ArrayList<>();
        list.add(100L);
        list.add(200L);
        list.add(210L);
        System.out.println();
    }
}
