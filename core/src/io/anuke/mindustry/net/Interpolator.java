package io.anuke.mindustry.net;


import arc.math.geom.Vec2;
import arc.util.Time;
import arc.math.Mathf;

public class Interpolator{
    //used for movement
    public Vec2 target = new Vec2();
    public Vec2 last = new Vec2();
    public float[] targets = {};
    public long lastUpdated, updateSpacing;

    //current state
    public Vec2 pos = new Vec2();
    public float[] values = {};

    public void read(float cx, float cy, float x, float y, long sent, float... target1ds){
        if(lastUpdated != 0) updateSpacing = Time.timeSinceMillis(lastUpdated);

        lastUpdated = Time.millis();

        targets = target1ds;
        last.set(cx, cy);
        target.set(x, y);
    }

    public void reset(){
        values = new float[0];
        targets = new float[0];
        target.setZero();
        last.setZero();
        lastUpdated = 0;
        updateSpacing = 16; //1 frame
        pos.setZero();
    }

    public void update(){

        /*
        if(pos.dst(target) > 128){
            pos.set(target);
            lastUpdated = 0;
            updateSpacing = 16;
        }*/

        if(lastUpdated != 0 && updateSpacing != 0){
            float timeSinceUpdate = Time.timeSinceMillis(lastUpdated);
            float alpha = Math.min(timeSinceUpdate / updateSpacing, 2f);

            pos.set(last).lerp(target, alpha);

            if(values.length != targets.length){
                values = new float[targets.length];
            }

            for(int i = 0; i < values.length; i++){
                values[i] = Mathf.slerp(values[i], targets[i], alpha);
            }
        }else{
            pos.set(target);
        }

    }
}