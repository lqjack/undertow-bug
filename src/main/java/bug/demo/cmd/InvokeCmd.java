package bug.demo.cmd;

import bug.demo.service.AuthClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class InvokeCmd  implements CommandLineRunner {

    @Autowired
    private OkHttpClient client;

    @Override
    public void run(String... args) throws Exception {

        Thread.sleep(TimeUnit.SECONDS.toMillis(10));

        //TODO should check the service has bootstraped.

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);

        List<Callable> calls = new ArrayList<>();
        for(int i =0;i<100;i++) {
            Random random = new Random();
            int param = random.nextInt(10000);
            calls.add(new Callable(param));
        }

        executorService.invokeAll(calls);
    }

    class Callable implements java.util.concurrent.Callable<AuthClient.Result> {
        private Call call;
        Callable(int param) {
            Request.Builder builder = new Request.Builder();
            builder.url("http://localhost:3333/zak/check?token=" + param);
            Request req = builder.build();
            call = client.newCall(req);
        }

        @Override
        public AuthClient.Result call() throws Exception {
            Response response = call.execute();
            if(response != null) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body().byteStream(), AuthClient.Result.class);
            }
            throw new IllegalArgumentException("cannot resolve the result");
        }
    }
}
