package net.fileme.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

@Data
public class DriveDto {

    @NotNull(groups = Update.class)
    private Long id;

    @NotEmpty
    @Pattern(regexp = "^(?=.{1,32}$)(?![_.])[\\p{L}\\p{Nd}_.-]+(?<![_.])$")
    private String dataName;

    @NotNull(groups = Update.class)
    private Integer dataType;

    private Integer accessLevel;
    @NotNull(groups = Create.class)
    private Long parentId;
    public interface Create extends Default{}
    public interface Update extends Default{}
}
