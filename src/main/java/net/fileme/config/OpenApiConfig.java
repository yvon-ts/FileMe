package net.fileme.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(SpringDocConfiguration.class)
public class OpenApiConfig {

    private static final String TOKEN_HEADER = "Authorization";
    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .components(
                        new Components().addSecuritySchemes(TOKEN_HEADER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("token"))
                ).addSecurityItem(new SecurityRequirement().addList(TOKEN_HEADER))
                .info(
                        new Info()
                                .title("FileMe API")
                                .description("FileMe系統API文件")
                                .contact(new Contact().name("Yvon Tseng").email("884ea103@gmail.com").url(""))
                                .version("0.1")
                );
    }
    @Bean
    public GroupedOpenApi publicApi(){
        return GroupedOpenApi.builder()
                .group("public API")
                .pathsToMatch("/pub/**", "/support/**", "/user/login")
                .build();
    }
    @Bean
    public GroupedOpenApi driveApi(){
        return GroupedOpenApi.builder()
                .group("drive API")
                .pathsToMatch("/drive/**")
                .build();
    }
    @Bean
    public GroupedOpenApi userApi(){
        return GroupedOpenApi.builder()
                .group("user API")
                .pathsToMatch("/user/**", "/support/**")
                .build();
    }
}
