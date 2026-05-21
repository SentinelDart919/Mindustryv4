package io.anuke.mindustry.content.blocks;

import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.UnitTypes;
import io.anuke.mindustry.game.ContentList;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.blocks.logic.LogicExporter;
import io.anuke.mindustry.world.blocks.logic.LogicImporter;
import io.anuke.mindustry.world.blocks.logic.MiningPost;

public class LogisticBlocks extends BlockList implements ContentList {
    public static Block miningPostT1, miningPostT2, logicExporter, logicImporter;
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
        miningPostT2 = new MiningPost("mining-post-t2"){{
            size = 3;
            health = 320 * size;
            postDrone = UnitTypes.minerDroneT2;
            itemCapacity = 100 * size;
            maxDrones = 3;
            ItemOptions = new Item[]{
                    Items.titanium,
                    Items.thorium,
                    Items.chromium,
            };
        }};

        logicExporter = new LogicExporter("logic-exporter"){{
            size = 3;
            health = 200;
            droneType = UnitTypes.logisticsDrone;
            maxDrones = 2;
        }};

        logicImporter = new LogicImporter("logic-importer"){{
            size = 3;
            health = 200;
        }};

    }
}
