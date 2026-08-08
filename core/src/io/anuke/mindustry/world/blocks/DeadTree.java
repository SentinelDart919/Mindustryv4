package io.anuke.mindustry.world.blocks;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.mindustry.content.fx.TreeFx;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.graphics.DrawPseudo3D;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.sounds.Sounds;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static io.anuke.mindustry.Vars.*;

public class DeadTree extends Prop{
    public float shadowOffset = -4f;
    /** Effect triggered when the tree collapses (bark flying off). */
    public Effect treeBreakEffect;
    /** Effect triggered when the stump is destroyed. */
    public Effect stumpBreakEffect;
    protected TextureRegion shadow, trunk, trunkShadow;

    public DeadTree(String name){
        super(name);
        solid = true;
        breakable = false;
        destructible = true;
        health = 500;
        update = true;
        targetable = false;
        living = true;
        layer = Layer.tree;
        damageWhenDeconstruct = true;
        deconstructDamagePercent = 20f;

        treeBreakEffect = TreeFx.stumpBreakEffect(90f, 30, 2, name + "-bark", 3f, 7f);
        stumpBreakEffect = TreeFx.stumpBreakEffect(90f, 12, 2, name + "-bark", 3f, 7f);
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
        return tile.entity instanceof DeadTreeEntity && ((DeadTreeEntity) tile.entity).stump;
    }

    @Override
    public void onDeconstructDamaged(Tile tile, boolean wasStump, float previousHealth, int builders){
        float amount = tile.block().health * deconstructDamagePercent / 100f * Math.max(1, builders);
        if(tile.entity instanceof DeadTreeEntity){
            DeadTreeEntity entity = (DeadTreeEntity) tile.entity;
            entity.stump = wasStump;
            entity.health = previousHealth;
            entity.damage(amount);
        }else if(tile.entity != null){
            tile.entity.health = previousHealth;
            tile.entity.damage(amount);
        }
    }

    @Override
    public TileEntity newEntity(){
        return new DeadTreeEntity();
    }

    @Override
    public void onDestroyed(Tile tile){
        super.onDestroyed(tile);

        if(stumpBreakEffect != null){
            Effects.effect(stumpBreakEffect, tile.worldx(), tile.worldy());
        }
    }

    @Override
    public void draw(Tile tile){
    }

    @Override
    public void drawLayer(Tile tile){
        boolean stump = isStumped(tile);

        float x = tile.worldx(), y = tile.worldy();
        float rotStatic = Mathf.randomSeed(tile.id(), 0, 4) * 90;

        //trunk below the tree, always drawn (same logic as the living tree)
        if(trunk != null){
            Draw.alpha(1f);
            Draw.rect(trunk, x, y, rotStatic);
        }

        if(!stump){
            float rot = rotStatic;
            float w = region.getRegionWidth(), h = region.getRegionHeight();
            float scl = 30f, mag = 0.2f;
            float height = 0.025f;
            float dx = DrawPseudo3D.xOffset(x, height);
            float dy = DrawPseudo3D.yOffset(y, height);

            Draw.alpha(1f);
            Draw.rectv(region, x + dx, y + dy, w, h, rot, vec -> vec.add(
                    Mathf.sin(vec.y * 3 + Timers.time(), scl, mag) + Mathf.sin(vec.x * 3 - Timers.time(), 70, 0.8f),
                    Mathf.cos(vec.x * 3 + Timers.time() + 8, scl + 6f, mag * 1.1f) + Mathf.sin(vec.y * 3 - Timers.time(), 50, 0.2f)
            ));
            Draw.alpha(1f);
        }
    }

    @Override
    public void drawShadow(Tile tile){
        float rot = Mathf.randomSeed(tile.id(), 0, 4) * 90;

        if(isStumped(tile)){
            Draw.rect(trunkShadow, tile.drawx(), tile.drawy(), size * tilesize, size * tilesize);
            return;
        }

        Draw.rect(shadow, tile.worldx() + shadowOffset, tile.worldy() + shadowOffset, rot);
    }

    @Override
    public void load(){
        super.load();
        shadow = Draw.region(name + "-shadow");
        trunk = Draw.region(name + "-trunk");
        trunkShadow = Draw.region("circle-shadow", Draw.region("shadow-1"));
    }

    public class DeadTreeEntity extends TileEntity{
        public boolean stump = false;
        public float stumpHealth = 0f;

        @Override
        public TileEntity init(Tile tile, boolean added){
            super.init(tile, added);
            stumpHealth = tile.block().health * 2f;
            return this;
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
        }

        @Override
        public void read(DataInput stream) throws IOException{
            stump = stream.readBoolean();
            stumpHealth = stream.readFloat();
        }
    }
}
