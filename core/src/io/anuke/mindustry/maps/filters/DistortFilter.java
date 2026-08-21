package io.anuke.mindustry.maps.filters;

import io.anuke.mindustry.world.Tile;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class DistortFilter extends GenerateFilter{
    public float scl = 40, mag = 5;

    @Override
    public String name(){
        return "Distort";
    }

    @Override
    public FilterOption[] options(){
        return new SliderOption[]{
            new SliderOption("scale", () -> scl, f -> scl = f, 1f, 200f),
            new SliderOption("magnitude", () -> mag, f -> mag = f, 0.5f, 100f)
        };
    }

    @Override
    public void apply(GenerateInput in){
        float nx = noise(in, scl, mag);
        float ny = noise(1, in, scl, mag);
        Tile tile = in.tile(in.x + nx - mag / 2f, in.y + ny - mag / 2f);
        if(tile != null){
            in.floor = tile.floor();
            if(!tile.block().synthetic() && !in.block.synthetic()){
                in.block = tile.block();
            }
        }
    }
}
