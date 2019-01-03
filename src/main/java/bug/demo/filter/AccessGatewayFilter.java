package bug.demo.filter;

import bug.demo.service.AuthClient;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@Slf4j
public class AccessGatewayFilter extends BaseFilter {

    private AuthClient authService;

    public AccessGatewayFilter() {
        super();
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return -1;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        boolean hasPermission = authService.checkPermission(request);
        if (hasPermission) {
            throw new IllegalArgumentException("401");
        }
        return null;
    }
}
