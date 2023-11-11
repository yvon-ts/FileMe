package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DataOwnerDto {
    @NotNull
    @Schema(description = "使用者ID",
            type = "string",
            example = "1710573934860890113")
    private Long userId;

    @NotNull
    @Schema(description = "檔案或目錄ID",
            type = "string",
            example = "1698350322036805633")
    private Long dataId;
}
