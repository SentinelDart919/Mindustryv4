package arc.entities.impl;

import arc.entities.trait.SolidTrait;
import arc.math.geom.Vec2;
import arc.util.Translator;

public abstract class SolidEntity extends BaseEntity implements SolidTrait{
    protected transient Vec2 velocity = new Translator(0f, 0.0001f);
    private transient Vec2 lastPosition = new Translator();

    @Override
    public Vec2 lastPosition(){ return lastPosition; }

    @Override
    public Vec2 getVelocity(){ return velocity; }
}