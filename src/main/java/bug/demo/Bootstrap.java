package bug.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@EnableFeignClients
@EnableCircuitBreaker
@ComponentScan(value = {"bug.demo"})
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }

    @ControllerAdvice
    public class RestExceptionHandler extends ResponseEntityExceptionHandler {
        @ExceptionHandler(value = Exception.class)
        @ResponseBody
        public ResponseEntity<Object> exceptionHandler(Exception e) {
            HashMap<String, Object> msg = new HashMap<>(2);
            msg.put("error", HttpStatus.PRECONDITION_FAILED.value());
            msg.put("message", "Something went wrong");
            logger.error(e.getMessage(),e);
            return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
        }
    }

}
