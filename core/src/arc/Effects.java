package arc;

import arc.struct.ObjectMap;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.entities.trait.PosTrait;
import arc.util.Tmp;

public class Effects{
    private static final ObjectMap<String, Effect> effects = new ObjectMap<>();
    private static float shakeFalloff = 1000f;
    private static EffectProvider provider = (effect, color, x, y, rotation, data) -> {};
    private static ScreenShakeProvider shakeProvider = (intensity, duration) -> {};

    public static Effect get(String name){
        return effects.get(name);
    }

    public static void setEffectProvider(EffectProvider provider){
        Effects.provider = provider;
    }

    public static void setScreenShakeProvider(ScreenShakeProvider shake){
        Effects.shakeProvider = shake;
    }

    public static void setShakeFalloff(float falloff){
        shakeFalloff = falloff;
    }

    public static void shake(float intensity, float duration){
        shakeProvider.shake(intensity, duration);
    }

    public static void shake(float intensity, float duration, PosTrait pos){
        //TODO maybe check distance
        shakeProvider.shake(intensity, duration);
    }

    public static void shake(float intensity, float duration, float x, float y){
        //TODO maybe check distance
        shakeProvider.shake(intensity, duration);
    }

    public static void effect(Effect effect, float x, float y){
        provider.effect(effect, effect.color, x, y, 0f, null);
    }

    public static void effect(Effect effect, float x, float y, float rotation){
        provider.effect(effect, effect.color, x, y, rotation, null);
    }

    public static void effect(Effect effect, Color color, float x, float y){
        provider.effect(effect, color, x, y, 0f, null);
    }

    public static void effect(Effect effect, float x, float y, float rotation, Object data){
        provider.effect(effect, effect.color, x, y, rotation, data);
    }

    public static void effect(Effect effect, Color color, float x, float y, float rotation, Object data){
        provider.effect(effect, color, x, y, rotation, data);
    }

    public static void effect(Effect effect, float color, float x, float y, float rotation){
        provider.effect(effect, Tmp.c1.set((int)color), x, y, rotation, null);
    }

    public static void effect(Effect effect, Color color, float x, float y, float rotation){
        provider.effect(effect, color, x, y, rotation, null);
    }

    public static void effect(Effect effect, Position pos){
        provider.effect(effect, effect.color, pos.getX(), pos.getY(), 0f, null);
    }

    public static void effect(Effect effect, Position pos, float rotation){
        provider.effect(effect, effect.color, pos.getX(), pos.getY(), rotation, null);
    }

    public static void effect(Effect effect, Color color, Position pos){
        provider.effect(effect, color, pos.getX(), pos.getY(), 0f, null);
    }

    public static void render(int id, Effect effect, Color color, float time, float rotation, float x, float y, Object data){
        provider.effect(effect, color, x, y, rotation, data);
    }

    public static class Effect{
        public final String name;
        public final Cons<EffectContainer> draw;
        public Color color = Color.white;
        public float size = 10f;
        public float lifetime = 30f;
        public boolean clip = true;
        public float layer = -1;
        public boolean rotating = true;

        public Effect(String name, float lifetime, float size, Cons<EffectContainer> draw){
            this.name = name;
            this.lifetime = lifetime;
            this.size = size;
            this.draw = draw;
            if(name != null) effects.put(name, this);
        }

        public Effect(String name, float lifetime, Cons<EffectContainer> draw){
            this(name, lifetime, 10f, draw);
        }

        public Effect(float lifetime, float size, Cons<EffectContainer> draw){
            this(null, lifetime, size, draw);
        }

        public Effect(float lifetime, Cons<EffectContainer> draw){
            this(null, lifetime, 10f, draw);
        }
    }

    public static class EffectContainer{
        public int id;
        public float x, y, time, rotation;
        public float lifetime;
        public Color color;
        public Object data;

        public void set(int id, float x, float y, float time, float rotation, float lifetime, Color color, Object data){
            this.id = id;
            this.x = x;
            this.y = y;
            this.time = time;
            this.rotation = rotation;
            this.lifetime = lifetime;
            this.color = color;
            this.data = data;
        }

        public float fin(){
            return time / lifetime;
        }

        public float fout(){
            return 1f - fin();
        }

        public float fslope(){
            return (0.5f - Math.abs(fin() - 0.5f)) * 2f;
        }

        public float finpow(){
            return (float)Math.pow(fin(), 3);
        }

        public void scaled(float length, Cons<EffectContainer> cons){
            float oldLifetime = lifetime;
            float oldTime = time;

            if(time <= length){
                lifetime = length;
                cons.get(this);
            }

            lifetime = oldLifetime;
            time = oldTime;
        }
    }

    public interface EffectProvider{
        void effect(Effect effect, Color color, float x, float y, float rotation, Object data);
    }

    public interface ScreenShakeProvider{
        void shake(float intensity, float duration);
    }
}
