package io.anuke.mindustry.maps.filters;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.content.blocks.OreBlocks;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.ucore.noise.Simplex;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class DefaultOresFilter extends GenerateFilter{
    private final Array<Item> oreItems = Array.with(
        Items.copper, Items.scrap, Items.coal,
        Items.lead, Items.titanium, Items.thorium, Items.chromium
    );

    @Override
    public String name(){
        return "Default Ores";
    }

    @Override
    public FilterOption[] options(){
        return new FilterOption[]{};
    }

    @Override
    public void apply(GenerateInput in){
        if(!(in.floor instanceof Floor) || !((Floor)in.floor).hasOres) return;
        if(in.block != Blocks.air) return;

        for(int i = oreItems.size - 1; i >= 0; i--){
            Item item = oreItems.get(i);
            Simplex noise = new Simplex(seed + i);

            if(noise.octaveNoise2D(1, 0.7, 1f / (4 + i * 2), in.x, in.y) / 4f +
                Math.abs(0.5f - noise.octaveNoise2D(2, 0.7, 1f / (50 + i * 2), in.x, in.y)) > 0.48f &&
                Math.abs(0.5f - noise.octaveNoise2D(1, 1, 1f / (55 + i * 4), in.x, in.y)) > 0.22f){

                Block oreBlock = OreBlocks.get(in.floor, item);
                if(oreBlock != null){
                    in.floor = oreBlock;
                    break;
                }
            }
        }
    }
}
