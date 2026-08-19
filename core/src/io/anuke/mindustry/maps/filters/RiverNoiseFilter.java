package io.anuke.mindustry.maps.filters;

import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.world.Block;
import io.anuke.ucore.noise.RidgedPerlin;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class RiverNoiseFilter extends GenerateFilter{
    public float scl = 40, threshold = 0f, threshold2 = 0.1f, octaves = 1, falloff = 0.5f;
    public Block floor = Blocks.water, floor2 = Blocks.deepwater, block = Blocks.air, target = Blocks.air;

    @Override
    public String name(){
        return "River";
    }

    @Override
    public FilterOption[] options(){
        return new FilterOption[]{
            new SliderOption("scale", () -> scl, f -> scl = f, 1f, 500f),
            new SliderOption("threshold", () -> threshold, f -> threshold = f, -1f, 1f),
            new SliderOption("threshold2", () -> threshold2, f -> threshold2 = f, -1f, 1f),
            new SliderOption("octaves", () -> octaves, f -> octaves = f, 1f, 10f),
            new SliderOption("falloff", () -> falloff, f -> falloff = f, 0f, 1f),
            new BlockOption("target", () -> target, b -> target = b, anyOptional),
            new BlockOption("block", () -> block, b -> block = b, wallsOptional),
            new BlockOption("floor", () -> floor, b -> floor = b, floorsOptional),
            new BlockOption("floor2", () -> floor2, b -> floor2 = b, floorsOptional)
        };
    }

    @Override
    public void apply(GenerateInput in){
        RidgedPerlin rid = new RidgedPerlin(seed + 1, (int)octaves);
        float n = rid.getValue((int)in.x, (int)in.y, 1f / scl);

        if(n >= threshold && (target == Blocks.air || in.floor == target || in.block == target)){
            if(floor != Blocks.air) in.floor = floor;
            if(in.block.solid && block != Blocks.air && in.block != Blocks.air){
                in.block = block;
            }
            if(n >= threshold2 && floor2 != Blocks.air){
                in.floor = floor2;
            }
        }
    }
}
