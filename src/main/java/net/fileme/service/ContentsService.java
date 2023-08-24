package net.fileme.service;

import java.util.List;
import java.util.Map;

/**
 * Contents = Folders + Files
 */
public interface ContentsService {

    Map<String, List<Object>> getContents(Long userId, Long folderId);
}
