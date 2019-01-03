package bug.demo.service;

import com.netflix.hystrix.HystrixCommand;
import feign.*;
import feign.hystrix.HystrixFeign;
import feign.okhttp.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.List;

@Component
public class AuthClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthClient.class);
    @Autowired
    private ApplicationContext context;
    @Autowired
    private okhttp3.OkHttpClient client;

    @Autowired
    private FallbackProvider fallbackProvider;

    public static class Result {
        String code;
        String message;

        public Result() {}

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        Result(String token) {
            this.code = token;
        }
    }

    interface TokenService {
        @RequestLine("GET /zak/check")
        Result check(@Param("token") String token);
    }

    interface GitHubHystrix {
        @RequestLine("GET /zak/check")
        HystrixCommand<Result> contributorsHystrixCommand(@Param("token") String token);
    }

    private String hostAndPort(URL url) {
        return "localhost:" + url.getPort();
    }

    public boolean checkPermission(HttpServletRequest request) {

//        getConfigInstance().setProperty(serverListKey(),
//                hostAndPort(server1.url("").url()) + "," + hostAndPort(
//                        server2.url("").url()));

        TokenService fallback = (token) -> {
            if (token == null || token.equals("")) {
                throw new RuntimeException("empty argument");
            } else {
                return new Result(token);
            }
        };

//        Client rawClient = null;
//        CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory = null;
//        SpringClientFactory springClientFactory = null;
//        LoadBalancerFeignClient lbfc = new LoadBalancerFeignClient(rawClient, cachingSpringLoadBalancerFactory, springClientFactory);

        TokenService api = HystrixFeign
                .builder()
                .requestInterceptor(requestTemplate -> {
                    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs == null) {
                        return;
                    }
                    HttpServletRequest req = attrs.getRequest();
                })
                .client(new OkHttpClient(client))
//                .client(lbfc)
                .target(TokenService.class, "http://localhost:3333", fallback);
        return request != null;
    }
}
