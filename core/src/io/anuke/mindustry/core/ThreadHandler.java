package io.anuke.mindustry.core;

import arc.Core;
import arc.util.Time;
import arc.util.Timers;

public class ThreadHandler{
    private long lastFrameTime;

    public ThreadHandler(){
        Time.setDeltaProvider(() -> {
            float result = Core.graphics.getDeltaTime() * 60f;
            return Float.isNaN(result) || Float.isInfinite(result) ? 1f : Math.min(result, 60f / 10f);
        });
    }

    public void run(Runnable r){
        r.run();
    }

    public void runGraphics(Runnable r){
        r.run();
    }

    public void runDelay(Runnable r){
        Core.app.post(r);
    }

    public long getFrameID(){
        return Core.graphics.getFrameId();
    }

    public void handleBeginRender(){
        lastFrameTime = Time.millis();
    }

    public void handleEndRender(){
        int fpsCap = Core.settings.getInt("fpscap", 125);

        if(fpsCap <= 120){
            long target = 1000/fpsCap;
            long elapsed = Time.timeSinceMillis(lastFrameTime);
            if(elapsed < target){
                try{
                    Thread.sleep(target - elapsed);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
