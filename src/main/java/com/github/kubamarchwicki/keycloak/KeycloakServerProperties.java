package com.github.kubamarchwicki.keycloak;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class KeycloakServerProperties {

    final Integer port;
    final String contextPath;
    final String adminUsername;
    final String adminPassword;
    final String configurationFile;

    KeycloakServerProperties(Properties properties) {
        port = Integer.valueOf(properties.getProperty("server.port"));
        contextPath = properties.getProperty("server.context-path");
        adminUsername = properties.getProperty("keycloak.adminUsername");
        adminPassword = properties.getProperty("keycloak.adminPassword");
        configurationFile = properties.getProperty("keycloak.configurationFile");

        Objects.requireNonNull(port);
        Objects.requireNonNull(contextPath);
        Objects.requireNonNull(adminUsername);
        Objects.requireNonNull(adminPassword);
        Objects.requireNonNull(configurationFile);
    }

    public static KeycloakServerProperties buildWithDefaults() {
        Properties properties = new Properties();
        try {
            properties.load(Runner.class.getResourceAsStream("/configuration.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new KeycloakServerProperties(properties);
    }
}
