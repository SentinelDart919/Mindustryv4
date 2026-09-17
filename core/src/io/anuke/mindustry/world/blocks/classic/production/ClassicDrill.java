package io.anuke.mindustry.world.blocks.classic.production;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.production.Drill;
import io.anuke.mindustry.world.meta.BlockGroup;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

public class ClassicDrill extends Drill{
    protected Item result;
    protected float time = 5;
    protected int capacity = 5;

    public ClassicDrill(String name){
        super(name);
        tier = 5;
        drillTime = time * 60f;
        itemCapacity = capacity;
        group = BlockGroup.drills;
    }

    public ClassicDrill result(Item result){
        this.result = result;
        return this;
    }

    public ClassicDrill time(float time){
        this.time = time;
        this.drillTime = time * 60f;
        return this;
    }

    public ClassicDrill cap(int capacity){
        this.capacity = capacity;
        this.itemCapacity = capacity;
        return this;
    }

    @Override
    public Item getDrop(Tile tile){
        return result;
    }

    @Override
    public boolean isValid(Tile tile){
        if(tile == null) return false;
        ItemStack drops = tile.floor().drops;
        return result != null && drops != null && drops.item == result;
    }

    @Override
    public TextureRegion[] getIcon(){
        return new TextureRegion[]{Draw.region(name)};
    }

    @Override
    public void draw(Tile tile){
        Draw.rect(region, tile.drawx(), tile.drawy());
    }

    @Override
    public void drawBloom(Tile tile){
    }

    @Override
    public boolean isLayer(Tile tile){
        return result != null && !isValid(tile);
    }

    @Override
    public void drawLayer(Tile tile){
        Draw.colorl(0.85f + Mathf.absin(Timers.time(), 6f, 0.15f));
        Draw.rect("cross", tile.worldx(), tile.worldy());
        Draw.color();
    }
}