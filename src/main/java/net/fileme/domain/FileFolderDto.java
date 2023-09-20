package net.fileme.domain;

import lombok.Data;
import net.fileme.domain.pojo.File;
import net.fileme.domain.pojo.Folder;

import java.util.List;

@Data
public class FileFolderDto {
    private List<File> files;
    private List<Folder> folders;
    private List<Long> fileIds;
    private List<Long> folderIds;
}
