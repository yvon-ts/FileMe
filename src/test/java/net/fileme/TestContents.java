package net.fileme;

import net.fileme.service.ContentsService;
import net.fileme.service.impl.ClientFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestContents {

    @Autowired
    private ContentsService contentsService;
    @Autowired
    private ClientFileServiceImpl clientFileService;
    @Test
    void testGetContents(){
        System.out.println(contentsService.getContents(0L, 0L));
    }
}
