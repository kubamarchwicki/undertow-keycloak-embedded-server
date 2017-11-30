package com.github.kubamarchwicki.keycloak.old;

//public class KeycloakStarter {
//
//    @Bean
//    ApplicationListener<ApplicationReadyEvent> onApplicationReadyEventListener(ServerProperties serverProperties) {
//        return evt -> {
//            ServletContext context = evt.getApplicationContext().getBean(ServletContext.class);
//            Object initializeFlag = context.getAttribute(EmbeddedKeycloakApplication.KEYCLOAK_INITIALIZED_FLAG);
//            if (initializeFlag == null) {
//                logger.error("Could not start Embedded Keycloak server");
//                evt.getApplicationContext().close();
//            }
//
//            Integer port = serverProperties.getPort();
//            String rootContextPath = serverProperties.getContextPath();
//
//            logger.info("Embedded Keycloak started: http://localhost:{}{} to use keycloak", port, rootContextPath);
//        };
//    }
//
//}
