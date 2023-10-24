package net.fileme.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.fileme.domain.dto.RoleDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleDtoMapper extends BaseMapper<RoleDto> {
    RoleDto getRolesByUserId(Long userId);
}
