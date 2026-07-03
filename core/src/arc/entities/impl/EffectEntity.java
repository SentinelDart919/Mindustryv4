package arc.entities.impl;

import arc.Effects;
import arc.Effects.Effect;
import arc.entities.trait.DrawTrait;
import arc.entities.trait.Entity;
import arc.graphics.Color;
import arc.util.pooling.Pools;

public class EffectEntity extends TimedEntity implements DrawTrait{
    public Effect effect;
    public Color color = Color.white;
    public Object data;
    public float rotation = 0f;
    public Entity parent;
    public float poffsetx, poffsety;

    public EffectEntity(){}

    public void setParent(Entity parent){
        this.parent = parent;
        this.poffsetx = x - parent.getX();
        this.poffsety = y - parent.getY();
    }

    @Override
    public float lifetime(){ return effect.lifetime; }

    @Override
    public float drawSize(){ return effect.size; }

    @Override
    public void update(){
        if(effect == null){
            remove();
            return;
        }
        super.update();
        if(parent != null){
            x = parent.getX() + poffsetx;
            y = parent.getY() + poffsety;
        }
    }

    @Override
    public void reset(){
        effect = null;
        color = Color.white;
        rotation = time = poffsetx = poffsety = 0f;
        parent = null;
        data = null;
    }

    @Override
    public void draw(){
        Effects.render(id, effect, color, time, rotation, x, y, data);
    }

    @Override
    public void removed(){
        Pools.free(this);
    }
}