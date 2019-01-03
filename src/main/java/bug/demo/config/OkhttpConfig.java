package bug.demo.config;

import feign.Feign;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
@ConditionalOnProperty(value = "feign.okhttp.enabled", matchIfMissing = true)
@Slf4j
public class OkhttpConfig {

    private static final Logger logger = LoggerFactory.getLogger(OkhttpConfig.class);
    private Charset UTF8 = Charset.forName("UTF-8");

    @Primary
    @Bean
    public OkHttpClient okHttpClient() {

        Interceptor logging = chain -> {

            Request request = chain.request();

            RequestBody requestBody = request.body();
            boolean hasRequestBody = requestBody != null;

            Connection connection = chain.connection();
            String requestStartMessage = "--> "
                    + request.method()
                    + ' ' + request.url()
                    + (connection != null ? " " + connection.protocol() : "");
            requestStartMessage += requestBody.contentLength() + "-byte body)";
            logger.info(requestStartMessage);

            if (hasRequestBody) {
                if (requestBody.contentType() != null) {
                    logger.info("Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    logger.info("Content-Length: " + requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    logger.info(name + ": " + headers.value(i));
                }
            }

            logger.info("--> END " + request.method());
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            logger.info("");
            logger.info(buffer.readString(charset));
            logger.info("--> END " + request.method()
                    + " (" + requestBody.contentLength() + "-byte body)");
            long startNs = System.nanoTime();
            Response response;
            try {
                response = chain.proceed(request);
            } catch (Exception e) {
                logger.info("<-- HTTP FAILED: " + e);
                throw e;
            }
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            ResponseBody responseBody = response.body();
            long contentLength = responseBody.contentLength();
            String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
            logger.info("<-- "
                    + response.code()
                    + (response.message().isEmpty() ? "" : ' ' + response.message())
                    + ' ' + response.request().url()
                    + " (" + tookMs + "ms" + (", " + bodySize + " body") + ')');

            for (int i = 0, count = headers.size(); i < count; i++) {
                logger.info(headers.name(i) + ": " + headers.value(i));
            }

            logger.info("<-- END HTTP");
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.

            Long gzippedLength = null;
            if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                gzippedLength = buffer.size();
                GzipSource gzippedResponseBody = null;
                try {
                    gzippedResponseBody = new GzipSource(buffer.clone());
                    buffer = new Buffer();
                    buffer.writeAll(gzippedResponseBody);
                } finally {
                    if (gzippedResponseBody != null) {
                        gzippedResponseBody.close();
                    }
                }
            }

            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            logger.info("");
            logger.info("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");

            if (contentLength != 0) {
                logger.info("");
                logger.info(buffer.clone().readString(charset));
            }

            if (gzippedLength != null) {
                logger.info("<-- END HTTP (" + buffer.size() + "-byte, "
                        + gzippedLength + "-gzipped-byte body)");
            } else {
                logger.info("<-- END HTTP (" + buffer.size() + "-byte body)");
            }

            return response;
        };

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.H2_PRIOR_KNOWLEDGE))
                .pingInterval(5, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);
        builder.addNetworkInterceptor(logging);
        builder.addNetworkInterceptor(chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.removeHeader("Content-Type");
            requestBuilder.addHeader("Accept", "application/json");
            requestBuilder.addHeader("Content-Type", "application/json");
            return chain.proceed(requestBuilder.build());
        });
        return builder.build();
    }
}