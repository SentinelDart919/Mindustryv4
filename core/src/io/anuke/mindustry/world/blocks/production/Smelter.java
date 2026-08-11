package io.anuke.mindustry.world.blocks.production;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.mindustry.content.fx.BlockFx;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.world.BarType;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.consumers.ConsumeItem;
import io.anuke.mindustry.world.consumers.ConsumeItems;
import io.anuke.mindustry.world.meta.BlockBar;
import io.anuke.mindustry.world.meta.BlockStat;
import io.anuke.mindustry.world.meta.StatUnit;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Fill;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.*;

public class Smelter extends Block{
    protected final int timerDump = timers++;
    protected final int timerSmoke = timers++;

    protected Item result;

    protected float minFlux = 0.2f;
    protected float fluxSpeedMult = 0.75f;
    protected float baseFluxChance = 0.25f;
    protected boolean useFlux = false;

    protected float craftTime = 20f;
    protected float burnDuration = 50f;
    protected Effect craftEffect = BlockFx.smelt, burnEffect = BlockFx.fuelburn;
    protected Color flameColor = Color.valueOf("ffb879");

    protected Effect smokeEffect = BlockFx.smokes;
    protected Color smokeColor = Color.valueOf("6d6d6d");
    protected float smokeLength = 20f;
    protected float smokeDirection = 180f;
    protected float smokeSize = 3f;
    protected float smokeInterval = 8f;
    protected float smokeRandomness = 1f;

    protected TextureRegion topRegion;

    public Smelter(String name){
        super(name);
        update = true;
        hasItems = true;
        solid = true;
        itemCapacity = 20;
        layerLight = true;
        setAmbientSound("loopSmelter", 0.09f);
        consumes.require(ConsumeItems.class);
        consumes.require(ConsumeItem.class);

    }

    @Override
    public void load(){
        super.load();
        topRegion = Draw.region(name + "-top", Draw.getClearRegion());
    }

    @Override
    public void setBars(){
        for(ItemStack item : consumes.items()){
            bars.add(new BlockBar(BarType.inventory, true, tile -> (float) tile.entity.items.get(item.item) / itemCapacity));
        }
    }

    @Override
    public void setStats(){
        super.setStats();

        if(consumes.has(ConsumeItem.class) && consumes.get(ConsumeItem.class).isOptional()){
            stats.remove(BlockStat.boostItem);
            stats.add(BlockStat.inputFuel, new ItemStack(consumes.item(), consumes.itemAmount()));
        }
        stats.add(BlockStat.fuelBurnTime, burnDuration / 60f, StatUnit.seconds);
        stats.add(BlockStat.outputItem, result);
        stats.add(BlockStat.craftSpeed, 60f / craftTime, StatUnit.itemsSecond);
        stats.add(BlockStat.craftTime, craftTime / 60f, StatUnit.seconds);
        stats.add(BlockStat.inputItemCapacity, itemCapacity, StatUnit.items);
        stats.add(BlockStat.outputItemCapacity, itemCapacity, StatUnit.items);
    }

    @Override
    public void init(){
        super.init();

        for(ItemStack item : consumes.items()){
            if(item.item.fluxiness >= minFlux && useFlux){
                throw new IllegalArgumentException("'" + name + "' has input item '" + item.item.name + "', which is a flux, when useFlux is enabled. To prevent ambiguous item use, either remove this flux item from the inputs, or set useFlux to false.");
            }
        }

        produces.set(result);
    }

    @Override
    public void update(Tile tile){
        SmelterEntity entity = tile.entity();

        if(entity.timer.get(timerDump, 5) && entity.items.has(result)){
            tryDump(tile, result);
        }

        //add fuel
        if(entity.consumed(ConsumeItem.class) && entity.burnTime <= 0f){
            entity.items.remove(consumes.item(), 1);
            entity.burnTime += burnDuration;
            Effects.effect(burnEffect, entity.x + Mathf.range(2f), entity.y + Mathf.range(2f));
            entity.ambientSoundEnabled = true;
        }

        //decrement burntime
        if(entity.burnTime > 0){
            entity.burnTime -= entity.delta();
            entity.heat = Mathf.lerpDelta(entity.heat, 1f, 0.02f);
            entity.ambientSoundEnabled = true;
            if(entity.timer.get(timerSmoke, smokeInterval)){
                Effects.effect(smokeEffect, tile.drawx() + Mathf.range(2f), tile.drawy() + Mathf.range(2f), 0f,
                        new BlockFx.SmokeData(smokeColor, smokeLength * size, smokeDirection, smokeSize * size, smokeRandomness));
            }
        }else{
            entity.heat = Mathf.lerpDelta(entity.heat, 0f, 0.02f);
            entity.ambientSoundEnabled = false;
        }

        //make sure it has all the items
        if(!entity.cons.valid()){
            return;
        }

        float baseSmeltSpeed = 1f;
        for(Item item : content.items()){
            if(item.fluxiness >= minFlux && tile.entity.items.get(item) > 0){
                baseSmeltSpeed = fluxSpeedMult;
                break;
            }
        }

        entity.craftTime += entity.delta();

        if(entity.items.get(result) >= itemCapacity //output full
                || entity.burnTime <= 0 //not burning
                || entity.craftTime < craftTime*baseSmeltSpeed){ //not yet time
            return;
        }

        entity.craftTime = 0f;

        boolean consumeInputs = true;

        if(useFlux){
            //remove flux materials if present
            for(Item item : content.items()){
                if(item.fluxiness >= minFlux && tile.entity.items.get(item) > 0){
                    tile.entity.items.remove(item, 1);

                    //chance of not consuming inputs if flux material present
                    consumeInputs = !Mathf.chance(item.fluxiness * baseFluxChance);
                    break;
                }
            }
        }

        if(consumeInputs){
            for(ItemStack item : consumes.items()){
                entity.items.remove(item.item, item.amount);
            }
        }
        offloadNear(tile, result);
        Effects.effect(craftEffect, flameColor, tile.drawx(), tile.drawy());
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        boolean isInput = false;

        for(ItemStack req : consumes.items()){
            if(req.item == item){
                isInput = true;
                break;
            }
        }

        return (isInput && tile.entity.items.get(item) < itemCapacity) || (item == consumes.item() && tile.entity.items.get(consumes.item()) < itemCapacity) ||
                (useFlux && item.fluxiness >= minFlux && tile.entity.items.get(item) < itemCapacity);
    }

    @Override
    public void draw(Tile tile){
        super.draw(tile);

        SmelterEntity entity = tile.entity();

        //draw glowing center
        if(entity.heat > 0f){
            float g = 0.1f;

            Draw.alpha(((1f - g) + Mathf.absin(Timers.time(), 8f, g)) * entity.heat);

            Draw.tint(flameColor);
            Fill.circle(tile.drawx(), tile.drawy(), 2f + Mathf.absin(Timers.time(), 5f, 0.8f));
            Draw.color(1f, 1f, 1f, entity.heat);
            Draw.rect(topRegion, tile.drawx(), tile.drawy());
            Fill.circle(tile.drawx(), tile.drawy(), 1f + Mathf.absin(Timers.time(), 5f, 0.7f));

            Draw.color();
        }
    }

    @Override
    public void drawLayerLight(Tile tile){
        SmelterEntity entity = tile.entity();
        if(entity.heat <= 0f) return;
        //pulse the light along with the smelter's flame effect
        float g = 0.1f;
        float pulse = (1f - g) + Mathf.absin(Timers.time(), 8f, g);
        drawLight(tile.drawx(), tile.drawy(), layerLightRadius * tilesize,
                layerLightOpacity * entity.heat * Mathf.clamp(pulse), flameColor);
    }

    @Override
    public TileEntity newEntity(){
        return new SmelterEntity();
    }

    public class SmelterEntity extends TileEntity{
        public float burnTime;
        public float heat;
        public float craftTime;
    }
}
