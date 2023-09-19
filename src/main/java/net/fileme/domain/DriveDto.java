package net.fileme.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
