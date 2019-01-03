package bug.demo.filter;

import com.netflix.zuul.ZuulFilter;

public abstract class BaseFilter extends ZuulFilter {
    protected String LOG_PRE = getClass().getSimpleName();
}
