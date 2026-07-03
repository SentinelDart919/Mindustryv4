package arc.entities.trait;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.Timers;

public interface TimeTrait extends ScaleTrait, Entity{
    float lifetime();
    void time(float time);
    float time();

    default void updateTime(){
        time(Mathf.clamp(time() + Timers.delta(), 0, lifetime()));
        if(time() >= lifetime()){
            remove();
        }
    }
}