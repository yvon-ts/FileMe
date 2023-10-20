package net.fileme.service;

import net.fileme.domain.Result;
import net.fileme.domain.dto.UserDto;

public interface LoginService {
    Result login(UserDto dto);
    Result logout();
}
