package net.fileme.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleDto {
    private Long userId;
    private List<String> roles;
}