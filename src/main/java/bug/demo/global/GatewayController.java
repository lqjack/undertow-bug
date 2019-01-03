package bug.demo.global;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewayController {
//    @Autowired
//    private Registration registration;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Map<String, Object> defaultMethod() {
        Map<String, Object> map = new HashMap<>();
//        map.put("serviceid",registration.getServiceId() );
        map.put("message","welcome to api gateway!");
        return map;
    }

}
