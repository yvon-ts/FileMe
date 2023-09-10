package net.fileme.domain.pojo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BasePojo {
    private Integer state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
