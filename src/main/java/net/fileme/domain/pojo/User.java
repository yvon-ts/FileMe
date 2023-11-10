package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

@Schema(description = "使用者")
@TableName("user_data")
@Data
public class User extends BasePojo{

    @TableId(value = "user_id")
    private Long id;

    @Schema(description = "帳號")
    @NotBlank
    @Pattern(regexp = "^(?=.{3,20}$)(?=.*[a-z])(?![_.])[a-z0-9._]+(?<![_.])$")
    private String username;

    @Schema(description = "密碼")
    @TableField(value = "pwd")
    @NotBlank
    @Pattern(groups = Create.class
    , regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-])[A-Za-z0-9!@#$%^&*()_+|~=?<>{}\\[\\]:;/.,-]{8,50}$")
    private String password;

    @Schema(description = "信箱")
    @Email
    private String email;

    private Integer membership;

    @Override // avoid password log
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", membership=" + membership +
                '}';
    }

    public interface Create extends Default{}
}
