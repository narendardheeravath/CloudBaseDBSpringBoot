package com.cloudbasedb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Configuration
public class OpenApiConfig {

    // ── CouchDB RestTemplate ──────────────────────────────────────────────────

    @Value("${couchdb.username:admin}")
    private String username;

    @Value("${couchdb.password:admin123}")
    private String password;

    @Value("${couchdb.protocol:http}")
    private String protocol;

    @Value("${couchdb.host:localhost}")
    private String host;

    @Value("${couchdb.port:5984}")
    private int port;

    @Value("${couchdb.database:products}")
    private String database;

    @Bean
    public RestTemplate couchDbRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        String credentials = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        ClientHttpRequestInterceptor authInterceptor =
                (request, body, execution) -> {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
                    return execution.execute(request, body);
                };
        restTemplate.setInterceptors(List.of(authInterceptor));
        return restTemplate;
    }

    @Bean
    public String couchDbBaseUrl() {
        return protocol + "://" + host + ":" + port + "/" + database;
    }

    // ── Swagger / OpenAPI ─────────────────────────────────────────────────────

    @Bean
    public OpenAPI cloudBaseDBOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CloudBaseDB Spring Boot API")
                        .description("Cloud-ready Product Management REST API backed by Apache CouchDB")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CloudBaseDB Team")
                                .email("support@cloudbasedb.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}

