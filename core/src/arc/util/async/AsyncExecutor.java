package arc.util.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncExecutor{
    private ExecutorService executor;

    public AsyncExecutor(int maxThreads){
        executor = Executors.newFixedThreadPool(maxThreads);
    }

    public void submit(Runnable task){
        executor.submit(task);
    }

    public void dispose(){
        executor.shutdown();
    }
}
