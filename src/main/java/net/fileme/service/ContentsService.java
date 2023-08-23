package net.fileme.service;

import java.util.List;
import java.util.Map;

/**
 * Contents = Folders + Files
 */
public interface ContentsService {

    /**
     * Get folders and files by folderId
     * @param userId
     * @param folderId
     * @return
     */
    Map<String, List<Object>> getContents(Long userId, Long folderId);
}
