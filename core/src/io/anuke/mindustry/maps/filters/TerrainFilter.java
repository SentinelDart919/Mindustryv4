package io.anuke.mindustry.maps.filters;

import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.world.Block;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class TerrainFilter extends GenerateFilter{
    public float scl = 40, threshold = 0.9f, octaves = 3f, falloff = 0.5f, magnitude = 1f, circleScl = 2.1f, tilt = 0f;
    public Block floor = Blocks.air;

    @Override
    public String name(){
        return "Terrain";
    }

    @Override
    public FilterOption[] options(){
        return new FilterOption[]{
            new SliderOption("scale", () -> scl, f -> scl = f, 1f, 500f),
            new SliderOption("magnitude", () -> magnitude, f -> magnitude = f, 0f, 2f),
            new SliderOption("threshold", () -> threshold, f -> threshold = f, 0f, 1f),
            new SliderOption("circle-scale", () -> circleScl, f -> circleScl = f, 0f, 3f),
            new SliderOption("octaves", () -> octaves, f -> octaves = f, 1f, 10f),
            new SliderOption("falloff", () -> falloff, f -> falloff = f, 0f, 1f),
            new SliderOption("tilt", () -> tilt, f -> tilt = f, -4f, 4f),
            new BlockOption("floor", () -> floor, b -> floor = b, floorsOptional)
        };
    }

    @Override
    public void apply(GenerateInput in){
        float n = noise(in.x, in.y + in.x * tilt, scl, magnitude, octaves, falloff)
            + Mathf.dst((float)in.x / in.width - 0.5f, (float)in.y / in.height - 0.5f) * circleScl;

        if(floor != Blocks.air && n >= threshold){
            in.floor = floor;
        }
    }
}
