package io.anuke.mindustry.content.blocks;

import io.anuke.mindustry.content.UnitTypes;
import io.anuke.mindustry.game.ContentList;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.blocks.storage.CoreBlock;
import io.anuke.mindustry.world.blocks.storage.HiveBlock;
import io.anuke.mindustry.world.blocks.storage.SortedUnloader;
import io.anuke.mindustry.world.blocks.storage.Vault;

public class StorageBlocks extends BlockList implements ContentList{
    public static Block core, vault, container, unloader, hive;

    @Override
    public void load(){
        core = new CoreBlock("core"){{
            health = 1100;
            defenseDrones = true;
            defenseDroneType = UnitTypes.defenseDrone;
            maxDefenseDrones = 3;
        }};

        vault = new Vault("vault"){{
            size = 3;
            itemCapacity = 900;
        }};

        container = new Vault("container"){{
            size = 2;
            itemCapacity = 200;
        }};

        unloader = new SortedUnloader("unloader"){{
            speed = 12f;
        }};

        hive = new HiveBlock("hive"){{
            size = 3;
            itemCapacity = 200000;
            health = 100000;
            droneType = UnitTypes.evilDraug;
            defenseDrones = true;
            maxDefenseDrones = 8;
            defenseDroneType = UnitTypes.evilSwarmDrone;
        }};
    }
}
