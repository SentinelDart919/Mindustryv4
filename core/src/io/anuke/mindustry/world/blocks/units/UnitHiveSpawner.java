package io.anuke.mindustry.world.blocks.units;

import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.meta.BlockFlag;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.EnumSet;
import io.anuke.ucore.util.Mathf;

/* TODO
*   make units spawns if the core has X amount of X materials - done but Needs Polishing
*   Add limits to Units (I should maybe copy how the limits works in UnitFactoryAdvanced.java)
* */
public class UnitHiveSpawner extends Block {
    public UnitType[] types;
    public ItemStack[][] consumerStacks;
    public float spawnTimer;
    public float minSpawnTimer;
    public float maxSpawnTimer;
    public UnitHiveSpawner(String name) {
        super(name);
        update = true;
        solid = false;
        flags = EnumSet.of(BlockFlag.producer, BlockFlag.target);
        maxSpawnTimer = 15f;
        minSpawnTimer = 5f;
    }

    public void spawn(Tile tile, Tile core, int spawn) {
        UnitHiveSpawnerEntity entity = tile.entity();
        int Random = Math.max(Mathf.random(0, consumerStacks.length) - 1 , 0);
        ItemStack[] consumer = consumerStacks[Random];
        if(core.entity.items.has(consumer)){
            for (int i = 0; i < consumer.length; i++) core.entity.items.remove(consumer[i]);
            BaseUnit unit = types[Random].create(tile.getTeam());
            unit.setSpawner(tile);
            unit.set(tile.drawx() + Mathf.range(4), tile.drawy() + Mathf.range(4));
            unit.add();
            unit.getVelocity().y = 0;
        }

    }

    /*@Override
    public void load() {
        super.load();
    }*/

    @Override
    public void update(Tile tile) {
        UnitHiveSpawnerEntity entity = tile.entity();
        Tile core = Vars.state.teams.get(tile.getTeam()).cores.first();
        spawnTimer += Timers.delta();
        if (spawnTimer >= Mathf.random(minSpawnTimer, maxSpawnTimer) * 60f) {
            spawnTimer = 0;
            spawn(tile, core, 1);
        }


    }

    public TileEntity newEntity() {
        return new UnitHiveSpawnerEntity();
    }


    public static class UnitHiveSpawnerEntity extends TileEntity {
        public int[] spawned;

    }
}
