package io.anuke.mindustry.maps.filters;

import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class BlendFilter extends GenerateFilter{
    public float radius = 2f;
    public Block block = Blocks.sand, floor = Blocks.sand, ignore = Blocks.air;

    @Override
    public String name(){
        return "Blend";
    }

    @Override
    public FilterOption[] options(){
        return new FilterOption[]{
            new SliderOption("radius", () -> radius, f -> radius = f, 1f, 10f),
            new BlockOption("block", () -> block, b -> block = b, anyOptional),
            new BlockOption("floor", () -> floor, b -> floor = b, anyOptional),
            new BlockOption("ignore", () -> ignore, b -> ignore = b, anyOptional)
        };
    }

    @Override
    public void apply(GenerateInput in){
        if(in.floor == block || block == Blocks.air || in.floor == ignore) return;

        int rad = (int)radius;
        boolean found = false;

        outer:
        for(int x = -rad; x <= rad; x++){
            for(int y = -rad; y <= rad; y++){
                if(x * x + y * y > rad * rad) continue;
                Tile tile = in.tile(in.x + x, in.y + y);
                if(tile != null && (tile.floor() == block || tile.block() == block)){
                    found = true;
                    break outer;
                }
            }
        }

        if(found){
            in.floor = floor;
        }
    }
}
