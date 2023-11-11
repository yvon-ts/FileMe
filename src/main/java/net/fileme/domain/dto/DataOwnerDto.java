package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "資料傳遞物件 使用者ID, 單個檔案或目錄")
@Data
public class DataOwnerDto {

    @Schema(description = "使用者ID",
            type = "string",
            example = "1710573934860890113")
    @NotNull
    private Long userId;

    @Schema(description = "檔案或目錄ID",
            type = "string",
            example = "1698350322036805633")
    @NotNull
    private Long dataId;
}
