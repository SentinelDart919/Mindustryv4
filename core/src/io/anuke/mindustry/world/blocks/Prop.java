package io.anuke.mindustry.world.blocks;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

public class Prop extends Block{
    protected TextureRegion[] shadowRegions, regions;
    protected int variants;
    /** When true, deconstructing this prop only plays out as a visual effect:
     *  the deconstruction event damages the block until it is destroyed instead of actually removing it. */
    public boolean damageWhenDeconstruct;
    /** Damage dealt per deconstruction completion, as a percentage of the block's max health.
     *  Lower values mean more deconstruction cycles are needed before the block is destroyed. */
    public float deconstructDamagePercent = 100f;

    public Prop(String name){
        super(name);
        breakable = true;
        alwaysReplace = true;
    }

    /** Whether this prop is already felled (e.g. a tree stump). Used to preserve state across a fake deconstruction. */
    public boolean isStumped(Tile tile){
        return false;
    }

    /** Damages the prop after the deconstruction animation finishes, instead of deconstructing it.
     *  'previousHealth' restores the prop's remaining health so partial damage accumulates across cycles,
     *  and 'builders' is the number of units (player + drones) that helped deconstruct, which multiplies the damage. */
    public void onDeconstructDamaged(Tile tile, boolean wasStump, float previousHealth, int builders){
        if(tile.entity != null){
            tile.entity.health = previousHealth;
            tile.entity.damage(tile.block().health * deconstructDamagePercent / 100f * Math.max(1, builders));
        }
    }

    @Override
    public void draw(Tile tile){
        if(variants > 0){
            Draw.rect(regions[Mathf.randomSeed(tile.id(), 0, Math.max(0, regions.length - 1))], tile.worldx(), tile.worldy());
        }else{
            Draw.rect(region, tile.worldx(), tile.worldy());
        }
    }

    @Override
    public void drawShadow(Tile tile){
        if(shadowRegions != null){
            Draw.rect(shadowRegions[(Mathf.randomSeed(tile.id(), 0, variants - 1))], tile.worldx(), tile.worldy());
        }else if(shadowRegion != null){
            Draw.rect(shadowRegion, tile.drawx(), tile.drawy());
        }
    }

    @Override
    public void load(){
        super.load();

        if(variants > 0){
            shadowRegions = new TextureRegion[variants];
            regions = new TextureRegion[variants];

            for(int i = 0; i < variants; i++){
                shadowRegions[i] = Draw.region(name + "shadow" + (i + 1));
                regions[i] = Draw.region(name + (i + 1));
            }
        }
    }

    @Override
    public TextureRegion getEditorIcon(){
        if(editorIcon == null){
            if(Core.atlas.hasRegion("block-icon-" + name)){
                editorIcon = Draw.region("block-icon-" + name);
            }else if(variants > 0 && regions != null){
                editorIcon = regions[0];
            }else{
                editorIcon = region;
            }
        }
        return editorIcon;
    }
}
