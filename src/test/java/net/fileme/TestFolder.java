package net.fileme;

import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.Folder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
@SpringBootTest
public class TestFolder {

    @Autowired
    private FolderMapper folderMapper;

    @Test
    void testInsertWithError(){
        try{
            Folder folder = new Folder();
            folder.setUserId(0L);
            folder.setFolderName("BB");
            folderMapper.insert(folder);
        }catch(DuplicateKeyException e){
            //throw 自定義error出來
            //前端alert即可
        }
    }
}
