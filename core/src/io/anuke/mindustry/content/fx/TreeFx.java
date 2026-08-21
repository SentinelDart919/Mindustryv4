package io.anuke.mindustry.content.fx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.core.Effects.EffectContainer;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

import java.util.Random;

/** Effects used by the living tree block, ported from the MineDusty mod. */
public class TreeFx{
    private static final Random rand = new Random();

    /** Wraps an effect so it is always drawn with the given color. Used for tinting tree debris with the tree's map color. */
    public static Effect colorEffect(Effect original, Color color){
        Effect result = new Effect(original.lifetime, e -> {
            e.color = color;
            original.draw.render(e);
        });
        result.size = original.size;
        return result;
    }

    /** Falling leaves, tinted with the tree's color. Region is resolved by name at render time. */
    public static Effect fallingLeaves(String texture){
        return new Effect(450f, 150f, e -> {
            Draw.color(e.color, e.color, fslope(e));
            Draw.alpha(fslope(e) * 3f);

            float drift = -20f * fin(e) * 4f;
            rand.setSeed(e.id);
            float ang = rand.nextFloat() * 360f;
            float len = 30f + finpow(e) * 40f;

            TextureRegion leaf = Draw.hasRegion(texture) ? Draw.region(texture) : null;
            if(leaf != null){
                Draw.rect(leaf,
                        e.x + MathUtils.cosDeg(ang) * len + drift,
                        e.y + MathUtils.sinDeg(ang) * len + drift,
                        16f, 16f, fin(e) * 360f);
            }
        });
    }

    /** Debris flying out when a tree falls. Pieces 2..max are tinted with the tree color, piece 1 is the pale stick. */
    public static Effect treeBreakEffect(float life, int quantity, int maxRegionId, String tex, float sizeDiv, float spawnRad){
        return new Effect(life, 45f, e -> {
            rand.setSeed(e.id);

            float cutThresh = 0.8f;
            float fade = fin(e) <= cutThresh ? 1f : 1f - Mathf.clamp((fin(e) - cutThresh) / (1f - cutThresh));
            int randCount = Mathf.randomSeed(e.id, quantity, quantity + 30);

            for(int i = 0; i < randCount; i++){
                float rot = e.rotation + randRange(-180f, 180f);
                int randRegion = rand.nextInt(maxRegionId) + 1;
                TextureRegion region = getRegion(tex, randRegion);
                if(region == null) continue;

                if(randRegion != 1){
                    Color base = e.color.cpy();
                    float darkFactor = randRange(0.5f, 1f);
                    base.r *= darkFactor;
                    base.g *= darkFactor;
                    base.b *= darkFactor;
                    Color faded = base.cpy();
                    faded.a *= fade;
                    Draw.color(base, faded, fin(e));
                }else{
                    Color white = Color.WHITE.cpy();
                    white.a *= fade;
                    Draw.color(Color.WHITE, white, fin(e));
                }

                float len = randRange(0f, 12f) * finpow(e);
                float fout = Math.max(fout(e), 0.5f);
                float size = fout * (region.getRegionWidth() / sizeDiv) + 0.8f;
                float rotFactor = rot + randRange(-180f, 180f) * pow2Out(Mathf.clamp(fin(e) / 0.8f, 0f, 1f));
                float spawnRadius = randRange(0f, spawnRad);

                Draw.rect(region,
                        e.x + MathUtils.cosDeg(rot) * spawnRadius + MathUtils.cosDeg(rot) * len * 4f,
                        e.y + MathUtils.sinDeg(rot) * spawnRadius + MathUtils.sinDeg(rot) * len * 4f,
                        size, size, rotFactor);
            }
        });
    }

    /** Bark chips flying out when a stump breaks. */
    public static Effect stumpBreakEffect(float life, int quantity, int maxRegionId, String tex, float sizeDiv, float spawnRad){
        return new Effect(life, 45f, e -> {
            rand.setSeed(e.id);

            float cutThresh = 0.8f;
            float fade = fin(e) <= cutThresh ? 1f : 1f - Mathf.clamp((fin(e) - cutThresh) / (1f - cutThresh));
            int randCount = Mathf.randomSeed(e.id, quantity, quantity + 5);

            for(int i = 0; i < randCount; i++){
                float rot = e.rotation + randRange(-180f, 180f);
                int randRegion = rand.nextInt(maxRegionId) + 1;
                TextureRegion region = getRegion(tex, randRegion);
                if(region == null) continue;

                Color white = Color.WHITE.cpy();
                white.a *= fade;
                Draw.color(Color.WHITE, white, fin(e));

                float len = randRange(0f, 12f) * finpow(e);
                float fout = Math.max(fout(e), 0.5f);
                float size = fout * (region.getRegionWidth() / (sizeDiv * randRange(0.8f, 1f))) + 0.8f;
                float rotFactor = rot + randRange(-180f, 180f) * pow2Out(Mathf.clamp(fin(e) / 0.8f, 0f, 1f));
                float spawnRadius = randRange(0f, spawnRad);

                Draw.rect(region,
                        e.x + MathUtils.cosDeg(rot) * spawnRadius + MathUtils.cosDeg(rot) * len * 4f,
                        e.y + MathUtils.sinDeg(rot) * spawnRadius + MathUtils.sinDeg(rot) * len * 4f,
                        size, size, rotFactor);
            }
        });
    }

    private static TextureRegion getRegion(String tex, int index){
        String name = tex + index;
        return Draw.hasRegion(name) ? Draw.region(name) : null;
    }

    private static float fin(EffectContainer e){
        return e.time / e.lifetime;
    }

    private static float fout(EffectContainer e){
        return 1f - fin(e);
    }

    private static float fslope(EffectContainer e){
        return 1f - Math.abs(2f * fin(e) - 1f);
    }

    private static float finpow(EffectContainer e){
        return 1f - (float)Math.pow(1f - fin(e), 3);
    }

    private static float pow2Out(float f){
        return 1f - (1f - f) * (1f - f);
    }

    private static float randRange(float min, float max){
        return min + rand.nextFloat() * (max - min);
    }
}
