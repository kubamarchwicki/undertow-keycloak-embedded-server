package com.github.kubamarchwicki.keycloak;

import org.jboss.logging.Logger;
import org.jboss.resteasy.core.Dispatcher;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.exportimport.ExportImportManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.ApplianceBootstrap;
import org.keycloak.services.resources.KeycloakApplication;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Consumer;

public class EmbeddedKeycloakApplication extends KeycloakApplication {

    public final static String KEYCLOAK_INITIALIZED_FLAG = "keycloak-initialized-flag";
    private final static Logger logger = Logger.getLogger(EmbeddedKeycloakApplication.class);
    private final KeycloakServerProperties keycloakServerProperties = KeycloakServerProperties.buildWithDefaults();
    private Consumer<InputStream> importUsers = stream -> {
        try {
            KeycloakSession session = getSessionFactory().create();
            Path keycloakTempDir = Files.createTempDirectory("keycloak-");
            Path tempFile = Files.createTempFile(keycloakTempDir, "keycloak-users-", ".json");

            long bytesCopied = Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            ExportImportConfig.setAction("import");
            ExportImportConfig.setProvider("singleFile");
            ExportImportConfig.setFile(tempFile.toString());

            ExportImportManager manager = new ExportImportManager(session);
            manager.runImport();
        } catch (Exception e) {
            logger.warn("Couldn't extract configuration file to import.");
        }
    };

    public EmbeddedKeycloakApplication(@Context ServletContext context, @Context Dispatcher dispatcher) throws IOException {
        super(context, dispatcher);

        tryCreateMasterRealmAdminUser();
        tryImportExistingKeycloakFile();

        context.setAttribute(KEYCLOAK_INITIALIZED_FLAG, true);
    }

    private void tryImportExistingKeycloakFile() throws IOException {
        Path maybeFile = Paths.get(keycloakServerProperties.configurationFile);
        if (Files.exists(maybeFile)) {
            logger.infof("Config file %s exists. Performing import", maybeFile);
            Optional.of(Files.newInputStream(maybeFile))
                    .ifPresent(importUsers);
        } else {
            URL url = EmbeddedKeycloakApplication.class.getResource("/" + keycloakServerProperties.configurationFile);
            try (InputStream is = url.openStream()) {
                logger.infof("Config file %s exists. Performing import", url);
                Optional.of(is).ifPresent(importUsers);
            } catch (Exception e) {
                logger.warnf("Couldn't extract configuration file %s for import.", url);
            }
        }
    }

    private void tryCreateMasterRealmAdminUser() {

        KeycloakSession session = getSessionFactory().create();

        ApplianceBootstrap applianceBootstrap = new ApplianceBootstrap(session);
        String adminUsername = keycloakServerProperties.adminUsername;
        String adminPassword = keycloakServerProperties.adminPassword;

        try {

            session.getTransactionManager().begin();
            applianceBootstrap.createMasterRealmUser(adminUsername, adminPassword);
            session.getTransactionManager().commit();
        } catch (Exception ex) {
            System.out.println("Couldn't create keycloak master admin user: " + ex.getMessage());
            session.getTransactionManager().rollback();
        }

        session.close();
    }

    public interface ExConsumer<T> {
        void apply(T t) throws Exception;
    }

}
