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

    /** Falling leaves, tinted with the tree's color. */
    public static Effect fallingLeaves(TextureRegion leaf){
        return new Effect(450f, 150f, e -> {
            Draw.color(e.color, e.color, fslope(e));
            Draw.alpha(fslope(e) * 3f);

            float drift = -80f * fin(e);
            rand.setSeed(e.id);
            float ang = rand.nextFloat() * 360f;
            float len = 30f + Mathf.pow(fin(e), 2f) * 40f;
            float x = e.x + MathUtils.cosDeg(ang) * len + drift;
            float y = e.y + MathUtils.sinDeg(ang) * len + drift;

            if(leaf != null){
                Draw.rect(leaf, x, y, 16f, 16f, fin(e) * 360f);
            }
        });
    }

    /** Debris flying out when a tree falls. Pieces 2..max are tinted with the tree color, piece 1 is the pale stick. */
    public static Effect treeBreakEffect(float life, int quantity, int maxRegionId, TextureRegion[] regions, float sizeDiv, float spawnRad){
        return new Effect(life, 45f, e -> {
            rand.setSeed(e.id);

            float cutThresh = 0.8f;
            float fade = fin(e) <= cutThresh ? 1f : 1f - Mathf.clamp((fin(e) - cutThresh) / (1f - cutThresh));
            int randCount = Mathf.randomSeed(e.id, quantity, quantity + 30);

            for(int i = 0; i < randCount; i++){
                float rot = e.rotation + randRange(-180f, 180f);
                int randRegion = rand.nextInt(maxRegionId) + 1;
                TextureRegion region = randRegion <= regions.length ? regions[randRegion - 1] : null;
                if(region == null) continue;

                if(randRegion != 1){
                    Color base = e.color.cpy();
                    float darkFactor = randRange(0.5f, 1f);
                    base.r *= darkFactor;
                    base.g *= darkFactor;
                    base.b *= darkFactor;
                    base.a *= fade;
                    Draw.color(base, Color.WHITE, fin(e));
                }else{
                    Color white = Color.WHITE.cpy();
                    white.a *= fade;
                    Draw.color(white, Color.WHITE, fin(e));
                }

                float len = randRange(0f, 12f) * Mathf.pow(fin(e), 2f);
                float fout = Math.max(fout(e), 0.5f);
                float size = fout * (region.getRegionWidth() / sizeDiv) + 0.8f;
                float rotFactor = rot + randRange(-180f, 180f) * pow2Out(Mathf.clamp(fin(e) / 0.8f));
                float spawnRadius = randRange(0f, spawnRad);

                Draw.rect(region,
                        e.x + MathUtils.cosDeg(rot) * spawnRadius + MathUtils.cosDeg(rot) * len * 4f,
                        e.y + MathUtils.sinDeg(rot) * spawnRadius + MathUtils.sinDeg(rot) * len * 4f,
                        size, size, rotFactor);
            }
        });
    }

    /** Bark chips flying out when a stump breaks. */
    public static Effect stumpBreakEffect(float life, int quantity, int maxRegionId, TextureRegion[] regions, float sizeDiv, float spawnRad){
        return new Effect(life, 45f, e -> {
            rand.setSeed(e.id);

            float cutThresh = 0.8f;
            float fade = fin(e) <= cutThresh ? 1f : 1f - Mathf.clamp((fin(e) - cutThresh) / (1f - cutThresh));
            int randCount = Mathf.randomSeed(e.id, quantity, quantity + 5);

            for(int i = 0; i < randCount; i++){
                float rot = e.rotation + randRange(-180f, 180f);
                int randRegion = rand.nextInt(maxRegionId) + 1;
                TextureRegion region = randRegion <= regions.length ? regions[randRegion - 1] : null;
                if(region == null) continue;

                Draw.color(Color.WHITE, Color.WHITE, fin(e));

                float len = randRange(0f, 12f) * Mathf.pow(fin(e), 2f);
                float fout = Math.max(fout(e), 0.5f);
                float size = fout * (region.getRegionWidth() / (sizeDiv * randRange(0.8f, 1f))) + 0.8f;
                float rotFactor = rot + randRange(-180f, 180f) * pow2Out(Mathf.clamp(fin(e) / 0.8f));
                float spawnRadius = randRange(0f, spawnRad);

                Draw.rect(region,
                        e.x + MathUtils.cosDeg(rot) * spawnRadius + MathUtils.cosDeg(rot) * len * 4f,
                        e.y + MathUtils.sinDeg(rot) * spawnRadius + MathUtils.sinDeg(rot) * len * 4f,
                        size, size, rotFactor);
            }
        });
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

    private static float pow2Out(float f){
        return 1f - (1f - f) * (1f - f);
    }

    private static float randRange(float min, float max){
        return min + rand.nextFloat() * (max - min);
    }
}
