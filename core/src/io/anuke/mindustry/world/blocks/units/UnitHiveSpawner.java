package io.anuke.mindustry.world.blocks.units;

import io.anuke.mindustry.world.Block;
/* TODO
*   make units spawns if the core has X amount of X materials
*   Subtract the materials from the nearest core(hive)
*   Add limits to Units (I should maybe copy how the limits works in UnitFactoryAdvanced.java)
*   random spawning timers
* */
public class UnitHiveSpawner extends Block {
    UnitHiveSpawner(String name) {
        super(name);
    }
}
