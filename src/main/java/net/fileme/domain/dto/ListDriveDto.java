package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ListDriveDto {
    @Schema(description = "ID (詳見個別API用途)", type = "string", example = "1710573934860890113")
    @NotNull
    private Long id;

    @Schema(description = "資料清單",
            example = "[{\"id\": \"1698350322036805633\", \"dataType\": \"0\"}, {\"id\": \"1716111892070346754\", \"dataType\": \"1\"}]")
    @NotNull
    private List<DriveDto> list;
}
