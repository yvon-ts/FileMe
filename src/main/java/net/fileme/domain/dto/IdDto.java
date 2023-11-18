package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "資料傳遞物件 ID")
@Data
public class IdDto {
    @Schema(description = "ID (詳見個別API用途)", type = "string", example = "1710573934860890113")
    @NotNull
    private Long id;
}
