package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "搜尋參數 關鍵字或分頁設定等")
@Data
public class SearchDto {
    @Schema(description = "關鍵字", type="array",
            implementation = java.lang.String.class,
            example = "['關鍵字1', '關鍵字2']")
    private List<String> keywords;
}
