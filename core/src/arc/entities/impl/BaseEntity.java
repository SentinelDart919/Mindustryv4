package arc.entities.impl;

import arc.entities.EntityGroup;
import arc.entities.trait.Entity;

public abstract class BaseEntity implements Entity{
    private static int lastid;
    public int id;
    public float x, y;
    protected transient EntityGroup group;

    public BaseEntity(){
        id = lastid++;
    }

    @Override
    public int getID(){ return id; }

    @Override
    public void resetID(int id){ this.id = id; }

    @Override
    public EntityGroup getGroup(){ return group; }

    @Override
    public void setGroup(EntityGroup group){ this.group = group; }

    @Override
    public float getX(){ return x; }

    @Override
    public void setX(float x){ this.x = x; }

    @Override
    public float getY(){ return y; }

    @Override
    public void setY(float y){ this.y = y; }

    @Override
    public String toString(){ return getClass() + " " + id; }
}