package bug.demo.config;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.RequestDumpingHandler;
import io.undertow.server.handlers.SetHeaderHandler;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class UndertowEnableHttp2Config {

    @Bean
    UndertowServletWebServerFactory undertowServletWebServerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        factory.setAccessLogDirectory(new File("."));
        factory.setAccessLogRotate(true);
        factory.setAccessLogPattern("[THREAD ID=%t] %t %a \"%r\" %s (%D ms) ");
        factory.setAccessLogPrefix("AUTH-");
        factory.setAccessLogEnabled(true);
        factory.setPort(8888);

        factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                        .setServerOption(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, false)
                        .setServerOption(UndertowOptions.IDLE_TIMEOUT,5*60*1000)
                        .setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT,5*60*1000),
                builder -> {
                });

        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            deploymentInfo.addInitialHandlerChainWrapper(RequestDumpingHandler::new);
            deploymentInfo.addInitialHandlerChainWrapper(next -> new SetHeaderHandler(next,"Content-Type","application/json"));
            deploymentInfo.addInitialHandlerChainWrapper(next -> new SetHeaderHandler(next,"Accept","application/json"));
        });

        return factory;
    }

}