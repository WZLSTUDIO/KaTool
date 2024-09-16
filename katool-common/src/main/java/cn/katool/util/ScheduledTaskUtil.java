package cn.katool.util;
import java.util.concurrent.*;

public class ScheduledTaskUtil {
    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(500,
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());
    public static ScheduledFuture<?> submitTask(Runnable run, long delay, TimeUnit timeUnit){
        ScheduledFuture<?> schedule = executor.scheduleWithFixedDelay(run, 0,delay, timeUnit);
        return schedule;
    }
    public static ScheduledFuture<?> submitTask(Callable run, long delay, TimeUnit timeUnit){
        ScheduledFuture<?> schedule = executor.scheduleWithFixedDelay(()->{
            try {
                run.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 0,delay, timeUnit);
        return schedule;
    }
    public static ScheduledFuture<?> submitTask(Runnable run,long initdeay, long delay, TimeUnit timeUnit){
        ScheduledFuture<?> schedule = executor.scheduleWithFixedDelay(run, initdeay,delay, timeUnit);
        return schedule;
    }
    public static ScheduledFuture<?> submitTask(Callable run,long initdeay,  long delay, TimeUnit timeUnit){
        ScheduledFuture<?> schedule = executor.scheduleWithFixedDelay(()->{
            try {
                run.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, initdeay,delay, timeUnit);
        return schedule;
    }
}
