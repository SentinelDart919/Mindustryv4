package io.anuke.mindustry.maps.filters;

import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.world.Block;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class ScatterFilter extends GenerateFilter{
    public float chance = 0.013f;
    public Block flooronto = Blocks.air, floor = Blocks.air, block = Blocks.air;

    @Override
    public String name(){
        return "Scatter";
    }

    @Override
    public FilterOption[] options(){
        return new FilterOption[]{
            new SliderOption("chance", () -> chance, f -> chance = f, 0f, 1f),
            new BlockOption("flooronto", () -> flooronto, b -> flooronto = b, floorsOptional),
            new BlockOption("floor", () -> floor, b -> floor = b, floorsOptional),
            new BlockOption("block", () -> block, b -> block = b, wallsOptional)
        };
    }

    @Override
    public void apply(GenerateInput in){
        if(block != Blocks.air && (in.floor == flooronto || flooronto == Blocks.air) && in.block == Blocks.air && chance(in.x, in.y) <= chance){
            in.block = block;
        }
        if(floor != Blocks.air && (in.floor == flooronto || flooronto == Blocks.air) && chance(in.x, in.y) <= chance){
            in.floor = floor;
        }
    }
}
