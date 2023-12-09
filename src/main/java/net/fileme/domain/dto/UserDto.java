package net.fileme.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

@Schema(description = "使用者資訊 帳密及信箱")
@Data
public class UserDto {
    @Schema(description = "帳號", example = "username")
    @NotNull
    @Pattern(regexp = "^(?=.{3,20}$)(?=.*[a-z])(?![_.])[a-z0-9._]+(?<![_.])$")
    private String username;

    @Schema(description = "密碼", example = "password")
    @NotNull
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-])[A-Za-z0-9!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-]{8,50}$")
    private String password;

    @Schema(description = "信箱", example = "example@email.com")
    @NotNull(groups = {Create.class, CheckEmail.class})
    @Email(groups = {Create.class, CheckEmail.class})
    private String email;
    @Override // avoid password log
    public String toString() {
        return "UserDto{" +
                "username='" + username + '\'' +
                '}';
    }
    public interface Create extends Default {}
    public interface CheckEmail {}
}
