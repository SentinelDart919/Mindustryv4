package arc.entities.impl;

import arc.entities.trait.ScaleTrait;
import arc.entities.trait.TimeTrait;

public abstract class TimedEntity extends BaseEntity implements ScaleTrait, TimeTrait{
    public float time;

    @Override
    public void time(float time){ this.time = time; }

    @Override
    public float time(){ return time; }

    @Override
    public void update(){ updateTime(); }

    public void reset(){ time = 0f; }

    @Override
    public float fin(){ return time() / lifetime(); }
}