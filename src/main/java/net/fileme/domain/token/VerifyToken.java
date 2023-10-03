package net.fileme.domain.token;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "token.verify")
public class VerifyToken extends BaseToken{
}
