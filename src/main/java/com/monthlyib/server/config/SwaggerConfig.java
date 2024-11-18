package com.monthlyib.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setDescription("dev");
        server.setUrl("https://monthlyib.suyoung-server.site");
        Server local = new Server();
        local.setDescription("local");
//        local.setUrl("http://localhost:8987");
        local.setUrl("http://localhost:8080");

        Server product = new Server();
        product.setDescription("product");
//        local.setUrl("http://localhost:8987");
        product.setUrl("https://monthlyib.server-get.site");

        Info info = new Info()
                .version("v0.0.1")
                .title("월간 IB API 명세")
                .description("월간 IB 백엔드 서버 API 명세서");

        // SecuritySecheme명
        String jwtSchemeName = "JWT Access Token";
        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("Bearer")
                        .bearerFormat("JWT")); // 토큰 형식을 지정하는 임의의 문자(Optional)

        OpenAPI result = new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
        result.setServers(List.of(product, server, local));

        return result;
    }

}
