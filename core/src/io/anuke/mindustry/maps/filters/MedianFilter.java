package io.anuke.mindustry.maps.filters;

import com.badlogic.gdx.utils.IntArray;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.world.Tile;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class MedianFilter extends GenerateFilter{
    private static final IntArray floors = new IntArray();

    public float radius = 2;
    public float percentile = 0.5f;

    @Override
    public String name(){
        return "Median";
    }

    @Override
    public FilterOption[] options(){
        return new SliderOption[]{
            new SliderOption("radius", () -> radius, f -> radius = f, 1f, 10f),
            new SliderOption("percentile", () -> percentile, f -> percentile = f, 0f, 1f)
        };
    }

    @Override
    public void apply(GenerateInput in){
        int rad = (int)radius;
        floors.clear();

        for(int x = -rad; x <= rad; x++){
            for(int y = -rad; y <= rad; y++){
                if(x * x + y * y > rad * rad) continue;
                Tile tile = in.tile(in.x + x, in.y + y);
                if(tile != null){
                    floors.add(tile.floor().id);
                }
            }
        }

        floors.sort();
        int floor = floors.get(Math.min((int)(floors.size * percentile), floors.size - 1));
        in.floor = Vars.content.block(floor);
    }
}
