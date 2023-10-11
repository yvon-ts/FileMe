package net.fileme.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto<T> {
    private Integer mapping;
    private String reqEmail;
    private T pending;
    private Integer issueNo;
}
