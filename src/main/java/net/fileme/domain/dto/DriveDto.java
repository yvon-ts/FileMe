package net.fileme.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DriveDto {

    @NotNull
    private Long id;
    @NotEmpty
//    @Pattern(regexp = "\\w")
    private String dataName;
    @NotNull
    private Integer dataType;
}
