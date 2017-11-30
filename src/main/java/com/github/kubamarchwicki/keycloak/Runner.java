package com.github.kubamarchwicki.keycloak;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.keycloak.services.filters.KeycloakSessionServletFilter;
import org.keycloak.services.listeners.KeycloakSessionDestroyListener;
import org.keycloak.services.resources.KeycloakApplication;

import javax.servlet.DispatcherType;

import static io.undertow.servlet.Servlets.*;

public class Runner {

    private final static Logger logger = Logger.getLogger(Runner.class);
    private final static String KEYCLOAK_APPLICATION_NAME = "embeddedKeycloakApplication";
    private final static String KEYCLOAK_APPLICATION_FILTER_NAME = "Keycloak Session Management";
    private final static KeycloakServerProperties keycloakServerProperties = KeycloakServerProperties.buildWithDefaults();

    public static void main(final String[] args) {

        Undertow server = null;
        try {
            DeploymentInfo servletBuilder = deployment()
                    .setClassLoader(Runner.class.getClassLoader())
                    .setContextPath(keycloakServerProperties.contextPath)
                    .setDeploymentName("keycloak-app.war")
                    .addServlets(servlet(KEYCLOAK_APPLICATION_NAME, HttpServlet30Dispatcher.class)
                            .addInitParam("keycloak.embedded", "true")
                            .addInitParam("javax.ws.rs.Application", EmbeddedKeycloakApplication.class.getName())
                            .addInitParam(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, "/")
                            .addInitParam(ResteasyContextParameters.RESTEASY_USE_CONTAINER_FORM_PARAMS, "true")
                            .addMapping("/*")
                            .setLoadOnStartup(1)
                            .setAsyncSupported(true)
                    )
                    .addListener(listener(KeycloakSessionDestroyListener.class))
                    .addFilter(filter(KEYCLOAK_APPLICATION_FILTER_NAME, KeycloakSessionServletFilter.class))
                    .addFilterUrlMapping(KEYCLOAK_APPLICATION_FILTER_NAME, "/*", DispatcherType.REQUEST); //DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.REQUEST, DispatcherType.ASYNC

            DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            HttpHandler servletHandler = manager.start();
            PathHandler path = Handlers.path(Handlers.redirect(keycloakServerProperties.contextPath))
                    .addPrefixPath(keycloakServerProperties.contextPath, servletHandler);

            server = Undertow.builder()
                    .addHttpListener(keycloakServerProperties.port, "localhost")
                    .setHandler(path)
                    .build();
            server.start();

            logger.infof("Embedded Keycloak started: http://localhost:%s%s to use keycloak",
                    keycloakServerProperties.port,
                    keycloakServerProperties.contextPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
