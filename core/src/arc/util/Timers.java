package arc.util;

import java.util.Iterator;
import arc.struct.Seq;
import arc.util.pooling.Pools;

public class Timers{
    private static double time;
    private static Seq<DelayRun> runs = new Seq<>();
    private static Seq<Long> marks = new Seq<>();

    public static synchronized void run(float delay, Runnable r){
        DelayRun run = Pools.obtain(DelayRun.class, DelayRun::new);
        run.finish = r;
        run.delay = delay;
        runs.add(run);
    }

    public static synchronized void runTask(float delay, Runnable r){
        run(delay, r);
    }

    public static float time(){ return (float)time; }

    public static void resetTime(float time){ Timers.time = time; }

    public static void mark(){ marks.add(Time.nanos()); }

    public static float delta(){ return Time.delta; }

    public static float deltaf(){ return Time.delta; }

    public static float deltaf(float scale){ return Time.delta * scale; }

    public static float elapsed(){
        if(marks.size == 0) return -1;
        return (Time.nanos() - marks.pop()) / 1000000f;
    }

    public static synchronized void update(){
        float delta = Time.delta;
        time += delta;

        Iterator<DelayRun> iter = runs.iterator();
        while(iter.hasNext()){
            DelayRun run = iter.next();
            run.delay -= delta;
            if(run.run != null) run.run.run();
            if(run.delay <= 0){
                if(run.finish != null) run.finish.run();
                iter.remove();
                Pools.free(run);
            }
        }
    }

    public static synchronized void clear(){ runs.clear(); }

    public static class DelayRun implements arc.util.pooling.Pool.Poolable{
        public float delay;
        public Runnable run;
        public Runnable finish;

        @Override
        public void reset(){
            delay = 0;
            run = finish = null;
        }
    }
}