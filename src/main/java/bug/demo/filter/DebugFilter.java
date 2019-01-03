package bug.demo.filter;

import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.pre.ServletDetectionFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class DebugFilter extends ServletDetectionFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DebugFilter.class);

    @Override
    public int filterOrder() {
        return -5;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        if(request.getRequestURI().contains("zak") && request.getParameter("zak") ==null) {
            LOG.error("catch it , thread id : {} ", Thread.currentThread().getId() + Thread.currentThread().getName());
        }


        return null;
    }
}
