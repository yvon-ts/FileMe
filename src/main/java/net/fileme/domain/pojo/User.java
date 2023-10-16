package net.fileme.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("user_data")
@Data
public class User extends BasePojo{

    @TableId(value = "user_id")
    private Long id;
    private String username;
    private String password;
    private String email;
    private Integer membership;
}
