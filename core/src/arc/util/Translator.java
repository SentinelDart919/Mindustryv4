package arc.util;

import arc.entities.trait.PosTrait;
import arc.math.Mathf;
import arc.math.geom.Vec2;

public class Translator extends Vec2 implements PosTrait{
    public Translator(){}

    public Translator(float x, float y){ super(x, y); }

    public Translator trns(float angle, float amount){
        set(amount, 0).rotate(angle);
        return this;
    }

    public Translator trns(float angle, float x, float y){
        set(x, y).rotate(angle);
        return this;
    }

    public Translator rnd(float length){
        setToRandomDirection().scl(length);
        return this;
    }

    public Translator set(PosTrait p){
        set(p.getX(), p.getY());
        return this;
    }

    @Override
    public float angle(){
        float angle = Mathf.atan2(x, y) * Mathf.radDeg;
        if(angle < 0) angle += 360;
        return angle;
    }

    @Override
    public float angleTo(float x, float y){
        return PosTrait.super.angleTo(x, y);
    }

    @Override
    public Vec2 rotateRad(float radians){
        float cos = Mathf.cos(radians);
        float sin = Mathf.sin(radians);
        float newX = this.x * cos - this.y * sin;
        float newY = this.x * sin + this.y * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }

    @Override
    public float getX(){ return x; }

    @Override
    public float getY(){ return y; }
}