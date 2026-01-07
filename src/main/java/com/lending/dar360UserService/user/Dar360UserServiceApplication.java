package com.lending.dar360UserService.user;

import com.lending.dar360UserService.user.util.AESUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.*;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.lending.dar360UserService.user.client.feign"})
@Slf4j
public class Dar360UserServiceApplication {
    @Value("${server.timezone}")
    private String timeZone;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(this.timeZone));
        log.info("\n\t TimeZone Default: {}", TimeZone.getDefault().getID());
        log.info("\n\t Datasource URL: {}", datasourceUrl);
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(
                Dar360UserServiceApplication.class);

        // Add initializer to override spring.datasource.url
        app.addInitializers(
                applicationContext -> {
                    ConfigurableEnvironment environment = applicationContext.getEnvironment();
                    applySecretDecryptionOverrides(environment);
                });

        app.run(args);
    }

    static void applySecretDecryptionOverrides(ConfigurableEnvironment environment) {
        applySecretDecryptionOverrides(
                environment,
                (encryptedText, secretKey) -> {
                    try {
                        return AESUtil.decrypt(encryptedText, secretKey);
                    } catch (Exception e) {
                        throw new SecretDecryptionException("Unable to decrypt secret", e);
                    }
                });
    }

    @FunctionalInterface
    interface SecretDecryptor {
        String decrypt(String encryptedText, String secretKey) throws SecretDecryptionException;
    }

    static class SecretDecryptionException extends Exception {
        SecretDecryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static void applySecretDecryptionOverrides(ConfigurableEnvironment environment, SecretDecryptor decryptor) {
        DecryptionConfig config = DecryptionConfig.from(environment);
        if (config == null) {
            return;
        }

        Map<String, Object> overrideProperties = decryptVariables(environment, decryptor, config);
        environment.getPropertySources().addFirst(
                new MapPropertySource("customDataSourceProperties", overrideProperties));
    }

    private static Map<String, Object> decryptVariables(
            ConfigurableEnvironment environment, SecretDecryptor decryptor, DecryptionConfig config) {
        Map<String, Object> overrideProperties = new HashMap<>();
        for (String propertyName : config.variableList()) {
            String encryptedValue = environment.getProperty(propertyName);
            if (encryptedValue == null) {
                continue;
            }
            try {
                String decryptedValue = decryptor.decrypt(encryptedValue, config.encryptionKey());
                overrideProperties.put(propertyName, decryptedValue);
            } catch (SecretDecryptionException e) {
                log.error("Error when decrypting", e);
            }
        }
        return overrideProperties;
    }

    private record DecryptionConfig(String encryptionKey, List<String> variableList) {
        static DecryptionConfig from(ConfigurableEnvironment environment) {
            String encryptionEnabled = environment.getProperty("app-config.secret-encryption.enabled");
            log.info("encryptionEnabled: {}", encryptionEnabled);
            if (!Boolean.parseBoolean(encryptionEnabled)) {
                return null;
            }

            String encryptionKey = environment.getProperty("app-config.secret-encryption.encryption-key");
            if (encryptionKey == null || encryptionKey.isBlank()) {
                log.warn("Secret encryption enabled but encryption key is missing/blank");
                return null;
            }

            String variables = environment.getProperty("app-config.secret-encryption.variables");
            if (variables == null || variables.isBlank()) {
                log.warn("Secret encryption enabled but variables list is missing/blank");
                return null;
            }

            List<String> variableList = parseVariables(variables);
            if (variableList.isEmpty()) {
                log.warn("Secret encryption enabled but variables list is empty after parsing");
                return null;
            }

            return new DecryptionConfig(encryptionKey, variableList);
        }

        private static List<String> parseVariables(String variables) {
            List<String> variableList = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(variables, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token == null) {
                    continue;
                }
                String trimmed = token.trim();
                if (!trimmed.isBlank()) {
                    variableList.add(trimmed);
                }
            }
            return variableList;
        }
    }
}
