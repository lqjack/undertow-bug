package bug.demo.config;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixInvokable;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class DeliveryHeaderHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryHeaderHystrixConcurrencyStrategy.class);
    private HystrixConcurrencyStrategy delegate;

    private Object wrap(Object obj) {
        if(obj == null) return "[null]";
        return obj;
    }

    public DeliveryHeaderHystrixConcurrencyStrategy() {
        try {
            this.delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
            if (this.delegate instanceof DeliveryHeaderHystrixConcurrencyStrategy) {
                return;
            }
            HystrixCommandExecutionHook commandExecutionHook =new HystrixCommandExecutionHook() {
                @Override
                public <T> void onStart(HystrixInvokable<T> commandInstance) {
                    logger.info("start : {} ", wrap(commandInstance));
                    super.onStart(commandInstance);
                }

                @Override
                public <T> T onEmit(HystrixInvokable<T> commandInstance, T value) {
                    logger.info("emit : {} , value  : {} ", wrap(commandInstance),wrap(value));
                    return super.onEmit(commandInstance, value);
                }

                @Override
                public <T> Exception onError(HystrixInvokable<T> commandInstance, HystrixRuntimeException.FailureType failureType, Exception e) {

                    logger.error("error : {} , failure type : {} , ex : {} ", wrap(commandInstance), wrap(failureType),wrap(e));

                    return super.onError(commandInstance, failureType, e);
                }

                @Override
                public <T> void onSuccess(HystrixInvokable<T> commandInstance) {

                    logger.info("success : {} ", wrap(commandInstance));
                    super.onSuccess(commandInstance);
                }

                @Override
                public <T> void onThreadStart(HystrixInvokable<T> commandInstance) {
                    logger.info("thread start : {} ", wrap(commandInstance));
                    super.onThreadStart(commandInstance);
                }

                @Override
                public <T> void onThreadComplete(HystrixInvokable<T> commandInstance) {
                    logger.info("thread complete : {} " , wrap(commandInstance));
                    super.onThreadComplete(commandInstance);
                }

                @Override
                public <T> void onExecutionStart(HystrixInvokable<T> commandInstance) {
                    logger.info("execution start : {} ", wrap(commandInstance));
                    super.onExecutionStart(commandInstance);
                }

                @Override
                public <T> T onExecutionEmit(HystrixInvokable<T> commandInstance, T value) {
                    logger.error("execution emit : {} , value : {} ",wrap(commandInstance),wrap(value));
                    return super.onExecutionEmit(commandInstance, value);
                }

                @Override
                public <T> Exception onExecutionError(HystrixInvokable<T> commandInstance, Exception e) {
                    logger.error("execution error : {}, ex : {} ", wrap(commandInstance),wrap(e));
                    return super.onExecutionError(commandInstance, e);
                }

                @Override
                public <T> void onExecutionSuccess(HystrixInvokable<T> commandInstance) {
                    logger.info("execution success : {} ", wrap(commandInstance));
                    super.onExecutionSuccess(commandInstance);
                }

                @Override
                public <T> void onFallbackStart(HystrixInvokable<T> commandInstance) {
                    logger.error("fall back start : {} ", wrap(commandInstance));
                    super.onFallbackStart(commandInstance);
                }

                @Override
                public <T> T onFallbackEmit(HystrixInvokable<T> commandInstance, T value) {
                    logger.error("fall back emit : {} ,value : {} ", wrap(commandInstance),wrap(value));
                    return super.onFallbackEmit(commandInstance, value);
                }

                @Override
                public <T> Exception onFallbackError(HystrixInvokable<T> commandInstance, Exception e) {
                    logger.error("fall back error : {} , ex : {} ", wrap(commandInstance),wrap(e));
                    return super.onFallbackError(commandInstance, e);
                }

                @Override
                public <T> void onFallbackSuccess(HystrixInvokable<T> commandInstance) {
                    logger.info("fall back success : {} ", wrap(commandInstance));
                    super.onFallbackSuccess(commandInstance);
                }

                @Override
                public <T> void onCacheHit(HystrixInvokable<T> commandInstance) {
                    logger.info("cache hit : {} ",wrap(commandInstance));
                    super.onCacheHit(commandInstance);
                }

                @Override
                public <T> void onUnsubscribe(HystrixInvokable<T> commandInstance) {
                    logger.info("unsubscibe : {} ", wrap(commandInstance));
                    super.onUnsubscribe(commandInstance);
                }

                @Override
                public <T> void onRunStart(HystrixCommand<T> commandInstance) {
                    logger.info("run start : {} ", wrap(commandInstance));
                    super.onRunStart(commandInstance);
                }

                @Override
                public <T> void onRunStart(HystrixInvokable<T> commandInstance) {
                    logger.info("run start : {} ", wrap(commandInstance));
                    super.onRunStart(commandInstance);
                }

                @Override
                public <T> T onRunSuccess(HystrixCommand<T> commandInstance, T response) {
                    logger.info("run success : {} , res : {}", wrap(commandInstance),wrap(response));
                    return super.onRunSuccess(commandInstance, response);
                }

                @Override
                public <T> T onRunSuccess(HystrixInvokable<T> commandInstance, T response) {
                    return super.onRunSuccess(commandInstance, response);
                }

                @Override
                public <T> Exception onRunError(HystrixCommand<T> commandInstance, Exception e) {
                    logger.error("run error : {} , ex : {} ", wrap(commandInstance),wrap(e));
                    return super.onRunError(commandInstance, e);
                }

                @Override
                public <T> Exception onRunError(HystrixInvokable<T> commandInstance, Exception e) {
                    logger.error("run error : {} , ex : {} ", wrap(commandInstance),wrap(e));
                    return super.onRunError(commandInstance, e);
                }

                @Override
                public <T> void onFallbackStart(HystrixCommand<T> commandInstance) {
                    super.onFallbackStart(commandInstance);
                }

                @Override
                public <T> T onFallbackSuccess(HystrixCommand<T> commandInstance, T fallbackResponse) {
                    return super.onFallbackSuccess(commandInstance, fallbackResponse);
                }

                @Override
                public <T> T onFallbackSuccess(HystrixInvokable<T> commandInstance, T fallbackResponse) {
                    return super.onFallbackSuccess(commandInstance, fallbackResponse);
                }

                @Override
                public <T> Exception onFallbackError(HystrixCommand<T> commandInstance, Exception e) {
                    return super.onFallbackError(commandInstance, e);
                }

                @Override
                public <T> void onStart(HystrixCommand<T> commandInstance) {
                    super.onStart(commandInstance);
                }

                @Override
                public <T> T onComplete(HystrixCommand<T> commandInstance, T response) {
                    return super.onComplete(commandInstance, response);
                }

                @Override
                public <T> T onComplete(HystrixInvokable<T> commandInstance, T response) {
                    return super.onComplete(commandInstance, response);
                }

                @Override
                public <T> Exception onError(HystrixCommand<T> commandInstance, HystrixRuntimeException.FailureType failureType, Exception e) {
                    logger.error("error : {} ,failure type : {} ,ex : {}", wrap(commandInstance),wrap(failureType),wrap(e));
                    return super.onError(commandInstance, failureType, e);
                }

                @Override
                public <T> void onThreadStart(HystrixCommand<T> commandInstance) {
                    logger.info("thread start : {} ", wrap(commandInstance));
                    super.onThreadStart(commandInstance);
                }

                @Override
                public <T> void onThreadComplete(HystrixCommand<T> commandInstance) {
                    logger.info("thread complete : {} ",wrap(commandInstance));
                    super.onThreadComplete(commandInstance);
                }
            };
            HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
            HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
            HystrixPropertiesStrategy propertiesStrategy =HystrixPlugins.getInstance().getPropertiesStrategy();
            if (logger.isDebugEnabled()) {
                logger.debug("Current Hystrix plugins configuration is [" + "concurrencyStrategy ["
                        + this.delegate + "]," + "eventNotifier [" + eventNotifier + "]," + "metricPublisher ["
                        + metricsPublisher + "]," + "propertiesStrategy [" + propertiesStrategy + "]," + "]");
                logger.debug("Registering Sleuth Hystrix Concurrency Strategy.");
            }
            HystrixPlugins.reset();
            HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
            HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
            HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
            HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
        } catch (Exception e) {
            logger.error("Failed to register Sleuth Hystrix Concurrency Strategy", e);
        }
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return new WrappedCallable<>(callable, requestAttributes);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
                                            HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize,
                                            HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return this.delegate.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime,
                unit, workQueue);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
                                            HystrixThreadPoolProperties threadPoolProperties) {
        return this.delegate.getThreadPool(threadPoolKey, threadPoolProperties);
    }

    @Override
    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return this.delegate.getBlockingQueue(maxQueueSize);
    }

    @Override
    public <T> HystrixRequestVariable<T> getRequestVariable(HystrixRequestVariableLifecycle<T> rv) {
        return this.delegate.getRequestVariable(rv);
    }

    static class WrappedCallable<T> implements Callable<T> {
        private final Callable<T> target;
        private final RequestAttributes requestAttributes;

        public WrappedCallable(Callable<T> target, RequestAttributes requestAttributes) {
            this.target = target;
            this.requestAttributes = requestAttributes;
        }

        @Override
        public T call() throws Exception {
            try {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                return target.call();
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    }
}