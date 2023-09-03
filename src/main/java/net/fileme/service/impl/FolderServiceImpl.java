package net.fileme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.fileme.domain.mapper.FolderMapper;
import net.fileme.domain.pojo.Folder;
import net.fileme.service.FolderService;
import org.springframework.stereotype.Service;

@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService {
}
