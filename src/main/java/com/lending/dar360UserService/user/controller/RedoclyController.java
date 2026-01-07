package com.lending.dar360UserService.user.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Hidden
@Slf4j
@RestController
public class RedoclyController {
    @Value("${springdoc.api-docs.path}")
    private String endpoint;

    @GetMapping("/redocly")
    public String redocly(HttpServletRequest request) {
        String response = null;
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("classpath:redocly/index.html");
            // Obtain an InputStream from the Resource
            try (InputStream inputStream = resource.getInputStream()) {
                // Use BufferedReader to read the content
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    String fullUrl = request.getRequestURL().toString();
                    String servletPath = request.getServletPath();
                    String baseUrl = fullUrl.replace(servletPath, "");

                    response = String.format(content.toString(), baseUrl
                            .concat(endpoint));
                }
            }
        } catch (IOException e) {
            log.error("Has error read resource content.", e);
            response = null;
        }
        return response;
    }
}

