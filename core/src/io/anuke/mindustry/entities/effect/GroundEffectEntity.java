package io.anuke.mindustry.entities.effect;

import arc.util.Timers;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.world.Tile;
import arc.Effects;
import arc.Effects.Effect;
import arc.util.Time;
import arc.entities.impl.EffectEntity;
import arc.func.Cons;
import arc.math.Mathf;
import arc.graphics.EffectRenderer;

/**
 * A ground effect contains an effect that is rendered on the ground layer as opposed to the top layer.
 */
public class GroundEffectEntity extends EffectEntity{
    private boolean once;

    @Override
    public void update(){
        GroundEffect effect = (GroundEffect) this.effect;

        if(effect.isStatic){
            time += Timers.delta();

            time = Mathf.clamp(time, 0, effect.staticLife);

            if(!once && time >= lifetime()){
                once = true;
                time = 0f;
                Tile tile = Vars.world.tileWorld(x, y);
                if(tile != null && tile.floor().isLiquid){
                    remove();
                }
            }else if(once && time >= effect.staticLife){
                remove();
            }
        }else{
            super.update();
        }
    }

    @Override
    public void draw(){
        GroundEffect effect = (GroundEffect) this.effect;

        if(once && effect.isStatic)
            Effects.render(id, effect, color, lifetime(), rotation, x, y, data);
        else
            Effects.render(id, effect, color, time, rotation, x, y, data);
    }

    @Override
    public void reset(){
        super.reset();
        once = false;
    }

    /**
     * An effect that is rendered on the ground layer as opposed to the top layer.
     */
    public static class GroundEffect extends Effect{
        /**
         * How long this effect stays on the ground when static.
         */
        public final float staticLife;
        /**
         * If true, this effect will stop and lie on the ground for a specific duration,
         * after its initial lifetime is over.
         */
        public final boolean isStatic;

        public GroundEffect(float life, float staticLife, Cons<Effects.EffectContainer> draw){
            super(life, draw);
            this.staticLife = staticLife;
            this.isStatic = true;
        }

        public GroundEffect(boolean isStatic, float life, Cons<Effects.EffectContainer> draw){
            super(life, draw);
            this.staticLife = 0f;
            this.isStatic = isStatic;
        }

        public GroundEffect(float life, Cons<Effects.EffectContainer> draw){
            super(life, draw);
            this.staticLife = 0f;
            this.isStatic = false;
        }
    }
}


