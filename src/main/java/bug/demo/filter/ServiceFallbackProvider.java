package bug.demo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public class ServiceFallbackProvider implements FallbackProvider {

    private static final Logger log = LoggerFactory.getLogger(ServiceFallbackProvider.class);

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {

        log.error("ServiceFallbackProvider.fallbackResponse:route={},cause={}",route,Throwables.getStackTraceAsString(cause));

        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.SERVICE_UNAVAILABLE;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return getStatusCode().value();
            }

            @Override
            public String getStatusText() throws IOException {
                return getStatusCode().getReasonPhrase();
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                RequestContext ctx = RequestContext.getCurrentContext();
                HttpServletRequest request = ctx.getRequest();
                String requestUri = request.getRequestURI();
                String errMsg = "["+requestUri+ "] is unavailable";
                Map<String,Object> params = Maps.newHashMap();
                params.put("errMsg", errMsg);
                params.put("errorCode", 500);
                ObjectMapper objectMapper = new ObjectMapper();
                String content = objectMapper.writeValueAsString(errMsg);
                return new ByteArrayInputStream(content.getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
                return httpHeaders;
            }
        };
    }

    @Override
    public String getRoute() {
        return "*";
    }
}