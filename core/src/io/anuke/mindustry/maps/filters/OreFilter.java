package io.anuke.mindustry.maps.filters;

import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.content.blocks.OreBlocks;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.mindustry.world.blocks.OreBlock;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class OreFilter extends GenerateFilter{
    public float scl = 23, threshold = 0.81f, octaves = 2f, falloff = 0.3f, tilt = 0f;
    public Block ore = OreBlocks.get(Blocks.stone, Items.copper), target = Blocks.air;

    @Override
    public String name(){
        return "Ore";
    }

    @Override
    public FilterOption[] options(){
        return new FilterOption[]{
            new SliderOption("scale", () -> scl, f -> scl = f, 1f, 500f),
            new SliderOption("threshold", () -> threshold, f -> threshold = f, 0f, 1f),
            new SliderOption("octaves", () -> octaves, f -> octaves = f, 1f, 10f),
            new SliderOption("falloff", () -> falloff, f -> falloff = f, 0f, 1f),
            new SliderOption("tilt", () -> tilt, f -> tilt = f, -4f, 4f),
            new BlockOption("ore", () -> ore, b -> ore = b, oresOnly),
            new BlockOption("target", () -> target, b -> target = b, oresCapable)
        };
    }

    @Override
    public void apply(GenerateInput in){
        float n = noise(in.x, in.y + in.x * tilt, scl, 1f, octaves, falloff);
        if(n > threshold && in.floor instanceof Floor && !(in.floor instanceof OreBlock)
            && ((Floor)in.floor).hasOres){
            boolean match = target == Blocks.air || in.floor == target;
            if(match){
                Item oreItem = ((OreBlock)ore).drops.item;
                Block oreBlock = OreBlocks.get(in.floor, oreItem);
                if(oreBlock != null){
                    in.floor = oreBlock;
                }
            }
        }
    }
}
