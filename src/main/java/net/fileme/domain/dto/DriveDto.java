package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

@Schema(description = "資料 目錄或檔案")
@Data
public class DriveDto {

    @Schema(description = "資料ID",
            type = "string",
            example = "1698350322036805633")
    @NotNull(groups = {Update.class, Read.class})
    private Long id;

    @Schema(description = "資料名稱",
            example = "範例名稱")
    @Pattern(regexp = "^(?=.{1,32}$)(?![_.])[\\p{L}\\p{Nd}_.-]+(?<![_.])$")
    @NotBlank
    private String dataName;

    @Schema(description = "資料種類 (0目錄，1檔案)",
            example = "0")
    @NotNull(groups = {Update.class, Read.class})
    private Integer dataType;

    @Schema(description = "資料權限 (0私人，1公開)",
            example = "1")
    private Integer accessLevel;

    @Schema(description = "所屬目錄ID (0根目錄， 999垃圾桶)",
            type = "string",
            example = "0")
    @NotNull(groups = Create.class)
    private Long parentId;

    public interface Create extends Default{}
    public interface Update extends Default{}
    public interface Read extends Default{}
}
