package net.fileme.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

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
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        ).addParameters(TOKEN_HEADER,
                                new Parameter()
                                        .in("header")
                                        .schema(new StringSchema())
                                        .name(TOKEN_HEADER)) // 原文tokenHeader
                )
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
                .group("公開API")
                .pathsToMatch("/pub/**")
                .build();
    }
    @Bean
    public GroupedOpenApi driveApi(){
        return GroupedOpenApi.builder()
                .group("drive API")
                .pathsToMatch("/drive/**")
                // 新增自定義config, 使用者認證的header???
                .addOperationCustomizer(((operation, handlerMethod) -> operation.security(
                        Collections.singletonList(new SecurityRequirement().addList(TOKEN_HEADER))
                ))).build();
    }
}
