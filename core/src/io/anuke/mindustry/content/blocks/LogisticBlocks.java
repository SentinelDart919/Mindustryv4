package io.anuke.mindustry.content.blocks;

import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.UnitTypes;
import io.anuke.mindustry.game.ContentList;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.blocks.production.MiningPost;

public class LogisticBlocks extends BlockList implements ContentList {
    public static Block miningPostT1, miningPostT2;
    @Override
    public void load() {
        miningPostT1 = new MiningPost("mining-post-t1"){{
            size = 2;
            health = 320;
            postDrone = UnitTypes.minerDroneT1;
            itemCapacity = 100;
            maxDrones = 5;
            ItemOptions = new Item[]{
                    Items.copper,
                    Items.lead,
                    Items.coal,
                    Items.scrap,
            };
        }};

    }
}
