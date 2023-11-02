package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

@TableName("user_data")
@Data
public class User extends BasePojo{

    @TableId(value = "user_id")
    private Long id;

    @NotBlank
    @Pattern(regexp = "^(?=.{3,20}$)(?=.*[a-z])(?![_.])[a-z0-9._]+(?<![_.])$")
    private String username;

    @TableField(value = "pwd")
    @NotBlank
    @Pattern(groups = Create.class
    , regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+|~\\-=?<>{}\\[\\]:;/.,])[A-Za-z0-9!@#$%^&*()_+|~\\-=?<>{}\\[\\]:;/.,]{8,50}$")
    private String password;

    @Email
    private String email;

    private Integer membership;

    public interface Create extends Default{}
}
