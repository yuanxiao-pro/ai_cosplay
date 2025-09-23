package edu.cust.secad.model.config.knife4j;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
                .group("chat-api")
                .pathsToMatch("/api/chat/**")
                .build();
    }
    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
                .group("common-api")
                .pathsToMatch("/api/common/**")
                .build();
    }

    @Bean
    public GroupedOpenApi chainApi() {
        return GroupedOpenApi.builder()
                .group("chain-api")
                .pathsToMatch("/api/chain/**")
                .build();
    }


    /***
     * @description 自定义接口信息
     */
    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("华佗API接口文档")
                        .version("1.0")
                        .description("华佗API接口文档")
                        .contact(new Contact().name("qy")));
    }


}
