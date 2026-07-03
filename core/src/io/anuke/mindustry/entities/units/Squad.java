package io.anuke.mindustry.entities.units;
import arc.util.Translator;

import arc.math.geom.Vec2;
import arc.math.geom.Vec2;

import static io.anuke.mindustry.Vars.threads;

/**
 * Used to group entities together, for formations and such.
 * Usually, squads are used by units spawned in the same wave.
 */
public class Squad{
    public Vec2 direction = new Translator();
    public int units;

    private long lastUpdated;

    protected void update(){
        if(threads.getFrameID() != lastUpdated){
            direction.setZero();
            lastUpdated = threads.getFrameID();
        }
    }
}
