package io.anuke.mindustry.world.blocks;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.anuke.mindustry.content.fx.TreeFx;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.effect.Fire;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.graphics.DrawPseudo3D;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.sounds.Sounds;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.core.Settings;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static io.anuke.mindustry.Vars.*;

/**
 * Unique tree block which renders trees in many layered passes (pseudo-3D parallax). When the fade setting is enabled,
 * trees fade out near the player, and falling leaves shower from the canopy. Burning trees eventually collapse into a
 * stump; the stump can be broken.
 * <p>
 * Ported from the MineDusty mod.
 */
public class LivingTree extends Prop{
    protected TextureRegion[] baseRegions, trunkRegions, topRegions, middleRegions, centerRegions, backRegions, shadowRegions;
    protected TextureRegion trunkShadow;
    public int variants = 2;
    public float shadowOffset = -4f;
    /** Rotates the tree shadow or not. */
    public boolean rotateShadow = true;
    /** Massive trees toggle (mainly layering fixes). */
    public boolean tallTree = false;
    /** Distance at which the tree starts and stops fading out. */
    public float fadeStart = 50f;
    public float fadeEnd = 15f;
    /** Effect for falling leaves. */
    public Effect effect;
    public float effectRange = 6f;
    /** Effects triggered when the tree falls and when its stump breaks. */
    public Effect treeBreakEffect;
    public Effect stumpBreakEffect;
    /** Tint color for leaves and debris. */
    public Color mapColor = Color.valueOf("7f8f4e");

    public LivingTree(String name){
        super(name);
        solid = true;
        size = 1;
        health = 500;
        update = true;
        destructible = true;
        breakable = false;
        alwaysReplace = false;
        targetable = false;
        living = true;
        layer = Layer.tree;
        damageWhenDeconstruct = true;
        deconstructDamagePercent = 20f;
        minimapColor = mapColor;

        effect = TreeFx.fallingLeaves("tree-prop3");
        treeBreakEffect = TreeFx.colorEffect(TreeFx.treeBreakEffect(120f, 45, 4, "tree-prop", 3.2f, 23f), mapColor);
        stumpBreakEffect = TreeFx.stumpBreakEffect(90f, 15, 2, "tree-bark", 3f, 7f);

        Settings.defaults("dusty-fade-enabled", true);
        Settings.defaults("dusty-fade-opacity", 80);
        Settings.defaults("dusty-fade-dist-multi", 1);
        Settings.defaults("dusty-toggle-mouse-fade", false);
        Settings.defaults("dusty-falling-leaves-enabled", true);
        Settings.defaults("dusty-falling-density", 25);
    }

    @Override
    public boolean canBreak(Tile tile){
        //deconstructing only plays out as a visual effect; in reality it damages the tree until it's destroyed
        return true;
    }

    @Override
    public boolean isStumped(Tile tile){
        if(BuildBlock.isFakeDeconstruct(tile)){
            return ((BuildBlock.BuildEntity) tile.entity).previousStump;
        }
        return tile.entity instanceof LivingTreeEntity && ((LivingTreeEntity) tile.entity).stump;
    }

    @Override
    public void onDeconstructDamaged(Tile tile, boolean wasStump, float previousHealth, int builders){
        float amount = tile.block().health * deconstructDamagePercent / 100f * Math.max(1, builders);
        if(tile.entity instanceof LivingTreeEntity){
            LivingTreeEntity entity = (LivingTreeEntity) tile.entity;
            entity.stump = wasStump;
            entity.health = previousHealth;
            entity.damage(amount);
        }else if(tile.entity != null){
            tile.entity.health = previousHealth;
            tile.entity.damage(amount);
        }
    }

    @Override
    public void load(){
        super.load();

        baseRegions = new TextureRegion[variants];
        trunkRegions = new TextureRegion[variants];
        topRegions = new TextureRegion[variants];
        middleRegions = new TextureRegion[variants];
        centerRegions = new TextureRegion[variants];
        backRegions = new TextureRegion[variants];
        shadowRegions = new TextureRegion[variants];

        for(int i = 0; i < variants; i++){
            baseRegions[i] = Draw.region(name + (i + 1));
            trunkRegions[i] = optionalRegion(name + "-trunk" + (i + 1));
            topRegions[i] = optionalRegion(name + "-top" + (i + 1));
            middleRegions[i] = optionalRegion(name + "-middle" + (i + 1));
            centerRegions[i] = optionalRegion(name + "-center" + (i + 1));
            backRegions[i] = optionalRegion(name + "-back" + (i + 1));
            shadowRegions[i] = optionalRegion(name + "-shadow" + (i + 1));
        }

        trunkShadow = Draw.region("circle-shadow", Draw.region("shadow-1"));
    }

    @Override
    public TextureRegion getEditorIcon(){
        if(editorIcon == null){
            editorIcon = baseRegions != null ? baseRegions[0] : region;
        }
        return editorIcon;
    }

    @Override
    public TextureRegion[] getBlockIcon(){
        //in the sprite generator, block.load() is never called, so fall back to the safe default
        if(baseRegions == null) return getIcon();

        return new TextureRegion[]{
                firstNonNull(shadowRegions),
                firstNonNull(backRegions),
                baseRegions[0],
                firstNonNull(centerRegions),
                firstNonNull(middleRegions),
                firstNonNull(topRegions)
        };
    }

    private TextureRegion firstNonNull(TextureRegion[] regions){
        return regions != null && regions.length > 0 && regions[0] != null ? regions[0] : Draw.region("clear");
    }

    private TextureRegion optionalRegion(String name){
        return Draw.hasRegion(name) ? Draw.region(name) : null;
    }

    @Override
    public TileEntity newEntity(){
        return new LivingTreeEntity();
    }

    @Override
    public void draw(Tile tile){
        //everything (trunk, base, parallax layers) is drawn in drawLayer, like DeadTree
    }

    @Override
    public void drawLayer(Tile tile){
        float fade = 1f;
        int variation = Mathf.randomSeed(tile.id(), 0, Math.max(0, baseRegions.length - 1));
        TextureRegion base = baseRegions[variation];
        if(base == null) return;

        float timeFactor = tallTree ? 0.3f : 1f;
        if(Settings.getBool("dusty-fade-enabled", true) && players.length > 0 && players[0] != null && !players[0].isDead()){
            float fadeOpacity = Settings.getInt("dusty-fade-opacity", 80) / 100f;
            float dst;
            float dstMulti = Settings.getInt("dusty-fade-dist-multi", 1);

            if(Settings.getBool("dusty-toggle-mouse-fade", false)){
                Vector2 mouse = Graphics.mouseWorld();
                dst = Mathf.dst(mouse.x - tile.worldx(), mouse.y - tile.worldy());
            }else{
                dst = Mathf.dst(players[0].x - tile.worldx(), players[0].y - tile.worldy());
            }

            fade = Mathf.clamp((dst - (fadeEnd * dstMulti)) / ((fadeStart * dstMulti) - (fadeEnd * dstMulti)), fadeOpacity, 1f);
        }

        float x = tile.worldx(), y = tile.worldy();
        float rotStatic = Mathf.randomSeed(tile.id(), 0, 4) * 90;
        float rot = rotStatic;
        float w = base.getRegionWidth(), h = base.getRegionHeight();
        float scl = 30f, mag = 0.2f;
        float baseHeight = 0.025f;

        boolean stump = isStumped(tile);

        //trunk below base layer
        if(trunkRegions[variation] != null){
            Draw.alpha(1f);
            Draw.rect(trunkRegions[variation], x, y, rotStatic);
        }

        if(!stump){
            //back leaves, behind the base layer
            if(backRegions[variation] != null){
                Draw.alpha(fade);
                Draw.rect(backRegions[variation], DrawPseudo3D.xHeight(x, baseHeight), DrawPseudo3D.yHeight(y, baseHeight), rot);
            }

            //base layer
            Draw.alpha(fade);
            Draw.rectv(base, DrawPseudo3D.xHeight(x, baseHeight), DrawPseudo3D.yHeight(y, baseHeight), w, h, rot, vec -> vec.add(
                    Mathf.sin(vec.y * 3 + Timers.time() * timeFactor, scl, mag) + Mathf.sin(vec.x * 3 - Timers.time() * timeFactor, 70, 0.8f),
                    Mathf.cos(vec.x * 3 + Timers.time() * timeFactor + 8, scl + 6f, mag * 1.1f) + Mathf.sin(vec.y * 3 - Timers.time() * timeFactor, 50, 0.2f)
            ));

            //center leaves
            if(centerRegions[variation] != null){
                float height = baseHeight + 0.006f * size;
                float drawX = DrawPseudo3D.xHeight(x, height);
                float drawY = DrawPseudo3D.yHeight(y, height);

                Draw.alpha(fade);
                Draw.rectv(centerRegions[variation], drawX, drawY, w, h, rot, vec -> vec.add(
                        Mathf.sin(vec.y * 2 + Timers.time() * timeFactor, scl, mag) + Mathf.sin(vec.x * 2 - Timers.time() * timeFactor, 70, 0.8f),
                        Mathf.cos(vec.x * 2 + Timers.time() * timeFactor + 8, scl + 6f, mag * 1.1f) + Mathf.sin(vec.y * 2 - Timers.time() * timeFactor, 50, 0.2f)
                ));
            }

            //middle leaves
            if(middleRegions[variation] != null){
                float height = baseHeight + 0.009f * size;
                float drawX = DrawPseudo3D.xHeight(x, height);
                float drawY = DrawPseudo3D.yHeight(y, height);

                Draw.alpha(fade);
                Draw.rectv(middleRegions[variation], drawX, drawY, w, h, rot, vec -> vec.add(
                        Mathf.sin(vec.y * 2 + Timers.time() * timeFactor, scl, mag) + Mathf.sin(vec.x * 2 - Timers.time() * timeFactor, 55, 0.9f),
                        Mathf.cos(vec.x * 2 + Timers.time() * timeFactor + 8, scl + 6f, mag * 1f) + Mathf.sin(vec.y * 2 - Timers.time() * timeFactor, 50, 0.2f)
                ));
            }

            //top leaves
            if(topRegions[variation] != null){
                float height = baseHeight + 0.012f * size;
                float drawX = DrawPseudo3D.xHeight(x, height);
                float drawY = DrawPseudo3D.yHeight(y, height);

                Draw.alpha(fade);
                Draw.rectv(topRegions[variation], drawX, drawY, w, h, rot, vec -> vec.add(
                        Mathf.sin(vec.y * 2 + Timers.time() * timeFactor, scl, mag) + Mathf.sin(vec.x * 2 - Timers.time() * timeFactor, 70, 0.8f),
                        Mathf.cos(vec.x * 2 + Timers.time() * timeFactor + 8, scl + 4f, mag * 1.4f) + Mathf.sin(vec.y * 2 - Timers.time() * timeFactor, 50, 0.2f)
                ));
            }

            Draw.alpha(1f);
        }

        //falling leaves effect
        if(state.isPaused()) return;
        int effectChance = Settings.getInt("dusty-falling-density", 25);
        if(Settings.getBool("dusty-falling-leaves-enabled", true) &&
                Mathf.chance((effectChance * 0.001f) * size * (tallTree ? 3f : 1f) * Timers.delta())){
            Effects.effect(effect, mapColor, x + Mathf.range(effectRange) * size, y + Mathf.range(effectRange) * size);
        }
    }

    /** The shadow is drawn through the shadow framebuffer (composited at 0.15 over the ground),
     *  exactly like the dead tree. Note: forest-interior trees (all 4 neighbors solid) are skipped
     *  by the engine, same as every other block that casts a framebuffer shadow. */
    @Override
    public void drawShadow(Tile tile){
        if(isStumped(tile)){
            Draw.rect(trunkShadow, tile.drawx(), tile.drawy(), size * tilesize, size * tilesize);
            return;
        }

        if(shadowRegions == null) return;
        int variation = Mathf.randomSeed(tile.id(), 0, Math.max(0, shadowRegions.length - 1));
        TextureRegion shadow = shadowRegions[variation];
        if(shadow == null) return;

        float x = tile.worldx() + shadowOffset, y = tile.worldy() + shadowOffset;
        if(rotateShadow){
            Draw.rect(shadow, x, y, Mathf.randomSeed(tile.id(), 0, 4) * 90);
        }else{
            Draw.rect(shadow, x, y);
        }
    }

    public class LivingTreeEntity extends TileEntity{
        public boolean stump = false;
        public float burnIntensity = 0f;
        public float stumpHealth = 0f;

        @Override
        public TileEntity init(Tile tile, boolean added){
            super.init(tile, added);
            stumpHealth = tile.block().health * 2f;
            return this;
        }

        @Override
        public void update(){
            boolean nearFire = hasFireNearby();
            if(nearFire){
                damage(Timers.delta() * 2f);
                burnIntensity = Mathf.clamp(burnIntensity + Timers.delta() * 0.001f, 0f, 1f);

                if(Mathf.chance(0.008f * Timers.delta())){
                    Tile nearby = world.tile(tile.x + Mathf.range(3), tile.y + Mathf.range(3));
                    if(nearby != null && nearby.entity != null){
                        Fire.create(nearby);
                    }
                }
            }else{
                burnIntensity = Mathf.clamp(burnIntensity - Timers.delta() * 0.0005f, 0f, 1f);
            }

            super.update();
        }

        /** Prevents the tree from collapsing during wave shockwaves. */
        @Override
        public void damage(float amount){
            if(amount >= 9e7f){
                return;
            }
            super.damage(amount);
        }

        @Override
        public void onDeath(){
            if(!stump){
                stump = true;
                health = stumpHealth;

                if(treeBreakEffect != null){
                    Effects.effect(treeBreakEffect, x, y, 8f * tile.block().size, null);
                }

                if(!headless && soundController != null && Sounds.destroyTree != null){
                    soundController.at(Sounds.destroyTree, x, y, 1f, 1f);
                }

                //the block itself doesn't change, so the shadow framebuffer would stay stale until the camera moves
                Events.fire(new TileChangeEvent(tile));
                return;
            }

            super.onDeath();
        }

        @Override
        public void write(DataOutput stream) throws IOException{
            stream.writeBoolean(stump);
            stream.writeFloat(stumpHealth);
            stream.writeFloat(burnIntensity);
        }

        @Override
        public void read(DataInput stream) throws IOException{
            stump = stream.readBoolean();
            stumpHealth = stream.readFloat();
            burnIntensity = stream.readFloat();
        }

        private boolean hasFireNearby(){
            if(fireGroup == null || fireGroup.isEmpty()) return false;

            float range = size * tilesize * 2f;
            for(Fire fire : fireGroup.all()){
                if(fire.isAdded() && Mathf.dst(fire.getX() - x, fire.getY() - y) < range){
                    return true;
                }
            }
            return false;
        }
    }
}
