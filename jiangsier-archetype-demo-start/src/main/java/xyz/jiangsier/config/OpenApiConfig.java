package xyz.jiangsier.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@SuppressWarnings("unused")
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI(
            @Value("${auth.token.headerName:X-API-TOKEN}") String securityHeaderName,
            @Value("${auth.token.parameterName:_token}") String securityParameterName
    ) {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("SysApiTokenHeader", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(securityHeaderName))
                        .addSecuritySchemes("SysApiTokenParameter", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.QUERY)
                                .name(securityParameterName)))
                .security(Collections.singletonList(new SecurityRequirement()
                        .addList("SysApiTokenHeader")
                        .addList("SysApiTokenParameter")))
                .info(new Info()
                        .title("jiangsier-archetype-demo OpenAPI definition")
                        .version("1.0.0-SNAPSHOT"));
    }
}
