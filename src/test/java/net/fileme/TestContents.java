package net.fileme;

import net.fileme.service.ContentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestContents {

    @Autowired
    private ContentsService contentsService;
    @Test
    void testGetContents(){
        System.out.println(contentsService.getContents(0L, 0L));
    }
}
