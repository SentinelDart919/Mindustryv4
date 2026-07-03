package io.anuke.mindustry.entities;

import arc.util.Time;

/**Replacement for arc.util.Timer - slot-based timer for entities. Auto-accumulates delta.*/
public class Timer{
    private final float[] times;

    public Timer(int capacity){
        this.times = new float[capacity];
    }

    /**Returns true if the slot has elapsed enough time, and resets it.*/
    public boolean get(int index, float duration){
        if(index < 0 || index >= times.length) return false;
        times[index] += Time.delta;
        if(times[index] >= duration){
            times[index] = 0f;
            return true;
        }
        return false;
    }

    public boolean get(int duration){
        return get(0, duration);
    }

    /**Returns the time elapsed in a slot.*/
    public float getTime(int index){
        if(index < 0 || index >= times.length) return 0f;
        return times[index];
    }

    /**Resets a specific timer.*/
    public void reset(int index, float value){
        if(index < 0 || index >= times.length) return;
        times[index] = value;
    }

    /**Resets all timers.*/
    public void reset(){
        for(int i = 0; i < times.length; i++){
            times[i] = 0f;
        }
    }
}
