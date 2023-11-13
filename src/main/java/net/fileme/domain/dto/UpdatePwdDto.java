package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UpdatePwdDto {
    @Schema(description = "舊密碼", example = "old-password")
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-])[A-Za-z0-9!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-]{8,50}$")
    private String oldPassword;

    @Schema(description = "新密碼", example = "new-password")
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-])[A-Za-z0-9!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-]{8,50}$")
    private String newPassword;
}
