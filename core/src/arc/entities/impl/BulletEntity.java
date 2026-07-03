package arc.entities.impl;

import arc.Effects;
import arc.entities.trait.*;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Timers;
import arc.util.pooling.Pool.Poolable;

public abstract class BulletEntity<T extends BaseBulletType> extends SolidEntity implements DamageTrait, ScaleTrait, Poolable, DrawTrait, VelocityTrait, TimeTrait{
    public T type;
    public Entity owner;
    public float time = 0f;
    public float rotation;
    public Object data;

    public BulletEntity(){}

    public BulletEntity(T type, Entity owner, float angle){
        this.type = type;
        this.owner = owner;
        velocity.set(0, type.speed()).setAngle(angle);
    }

    @Override
    public void getHitbox(Rect rectangle){
        rectangle.setSize(type.hitSize()).setCenter(x, y);
    }

    @Override
    public void getHitboxTile(Rect rectangle){
        rectangle.setSize(type.hitSize()).setCenter(x, y);
    }

    @Override
    public float lifetime(){ return type.lifetime(); }

    @Override
    public void time(float time){ this.time = time; }

    @Override
    public float time(){ return time; }

    public float getRotation(){ return angle(); }

    public void setRotation(float rotation){ velocity.setAngle(rotation); }

    @Override
    public void update(){
        type.update(this);
        rotation = velocity.angle();
        x += velocity.x * Timers.delta();
        y += velocity.y * Timers.delta();
        velocity.scl(1f - type.drag() * Timers.delta());
        updateLife();
    }

    protected void updateLife(){
        time += Timers.delta();
        time = Mathf.clamp(time, 0, type.lifetime());
        if(time >= type.lifetime()){
            type.despawned(this);
            remove();
        }
    }

    public Entity getOwner(){ return owner; }

    @Override
    public float drawSize(){ return type.drawSize(); }

    @Override
    public void added(){ type.init(this); }

    @Override
    public float getDamage(){ return type.damage(); }

    @Override
    public void draw(){ type.draw(this); }

    @Override
    public boolean collides(SolidTrait other){
        return other != owner && !(other instanceof DamageTrait);
    }

    @Override
    public void collision(SolidTrait other, float x, float y){
        if(!type.pierce()) remove();
        type.hit(this, x, y);
    }

    @Override
    public float fin(){ return time / type.lifetime(); }

    @Override
    public Vec2 getVelocity(){ return velocity; }

    @Override
    public void reset(){
        type = null;
        owner = null;
        velocity.setZero();
        time = 0f;
    }

    public void setVelocity(float speed, float angle){
        velocity.set(0, speed).setAngle(angle);
    }

    public void limit(float f){ velocity.limit(f); }

    public void setAngle(float angle){ velocity.setAngle(angle); }

    public float angle(){
        float angle = Mathf.atan2(velocity.x, velocity.y) * Mathf.radDeg;
        if(angle < 0) angle += 360;
        return angle;
    }
}