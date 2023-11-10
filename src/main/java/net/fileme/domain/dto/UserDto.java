package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Schema(description = "使用者帳密")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Schema(description = "帳號")
    @NotBlank
    @Pattern(regexp = "^(?=.{3,20}$)(?=.*[a-z])(?![_.])[a-z0-9._]+(?<![_.])$")
    private String username;

    @Schema(description = "密碼")
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-])[A-Za-z0-9!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-]{8,50}$")
    private String password;
    @Override // avoid password log
    public String toString() {
        return "UserDto{" +
                "username='" + username + '\'' +
                '}';
    }
}
