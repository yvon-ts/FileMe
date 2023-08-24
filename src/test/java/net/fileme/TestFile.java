package net.fileme;

import net.fileme.mapper.FileMapper;
import net.fileme.pojo.File;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestFile {

    @Autowired
    private FileMapper fileMapper;
    @Test
    void testInsert(){
        File file = new File();
        file.setUserId(0L);
        file.setFileName("A測試檔案");
        file.setExt("png");
        file.setSize(123L);
        fileMapper.insert(file);
        List<Map<String, Object>> maps = fileMapper.selectMaps(null);
        System.out.println(maps);
    }
}
