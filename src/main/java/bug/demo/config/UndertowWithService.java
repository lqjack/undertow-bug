package bug.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import io.undertow.servlet.api.ServletInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class UndertowWithService {

    @Autowired
    private UndertowServletWebServerFactory factory;

    @Bean
    Servlet tokenServlet() {
        return new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

                if(req.getRequestURI().equals("/zak/check")) {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

                        String token = req.getParameter("token");
                        if(token == null || token.equals("")) {
                            throw new IllegalArgumentException("token is empty.");
                        }

                        ObjectMapper mapper = new ObjectMapper();

                        Map<String,Object> params = Maps.newHashMap();

                        params.put("code",token);
                        params.put("message","token exist");
                        resp.setHeader("Content","application/json");
                        resp.setHeader("Accecpt","application/json");
                        resp.getWriter().print(mapper.writeValueAsString(params));
                        resp.getWriter().flush();

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e.getCause());
                    }
                }
                super.doGet(req, resp);
            }
        };
    }

    public void request() {
        factory.setPort(3333);
        factory.addDeploymentInfoCustomizers(custom -> {
            custom.addServlets(new ServletInfo("tokenService",tokenServlet().getClass()));
        });
        WebServer webServer = factory.getWebServer();

        webServer.start();

    }
}
