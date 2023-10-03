package net.fileme.domain.token;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Data
@Configuration
public class BaseToken {
    private Integer exp;
    private TimeUnit timeUnit;
    private String description;
    private String mailSubject;
}
