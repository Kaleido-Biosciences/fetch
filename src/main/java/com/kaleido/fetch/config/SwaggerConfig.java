package com.kaleido.fetch.config;

import com.google.common.net.HttpHeaders;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${fetch.endpoint}")
    private String fetchEndpoint;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.kaleido.fetch.resource"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Collections.singletonList(new ApiKey(
                        "Token Access",
                        HttpHeaders.AUTHORIZATION,
                        In.HEADER.name())))
                .securityContexts(Collections.singletonList(securityContext()))
                .host(fetchEndpoint)
                .apiInfo(metaData());
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.ant("/**"))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("ADMIN", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference("Token Access", authorizationScopes));
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
                .title("Atlas Search Endpoint")
                .description("This is a sample of what Atlas is expecting from an endpoint, to fill it with components.")
                .version("1.0.0")
                .contact(new Contact("Atlas", "fetch.apps.kaleidobio.com", "kaleido-oss@glycobiome.onmicrosoft.com"))
                .build();
    }

}
