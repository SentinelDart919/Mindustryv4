package io.anuke.mindustry.world.blocks.units;

import com.badlogic.gdx.graphics.Color;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.meta.BlockFlag;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Lines;
import io.anuke.ucore.util.EnumSet;
import io.anuke.ucore.util.Geometry;
import io.anuke.ucore.util.Mathf;

/* TODO
*   make units spawns if the core has X amount of X materials - done but Needs Polishing
*   Add limits to Units (I should maybe copy how the limits works in UnitFactoryAdvanced.java)
* */
public class UnitHiveSpawner extends Block {
    public UnitType[] types;
    public ItemStack[][] consumerStacks;
    public float minSpawnTimer;
    public float maxSpawnTimer;
    public UnitHiveSpawner(String name) {
        super(name);
        update = true;
        solid = false;
        flags = EnumSet.of(BlockFlag.producer, BlockFlag.target);
        layer = Layer.back;
        maxSpawnTimer = 15f;
        minSpawnTimer = 5f;
    }

    public void spawn(Tile tile, Tile core) {
        UnitHiveSpawnerEntity entity = tile.entity();
        int random = Mathf.random(0, types.length - 1);
        ItemStack[] consumer = consumerStacks[Math.min(random, consumerStacks.length - 1)];
        if(core.entity.items.has(consumer)){
            for (ItemStack itemStack : consumer) core.entity.items.remove(itemStack);
            BaseUnit unit = types[random].create(tile.getTeam());
            unit.setSpawner(tile);
            unit.set(tile.drawx() + Mathf.range(Vars.tilesize * 2), tile.drawy() + Mathf.range(Vars.tilesize * 2));
            unit.add();
        }
    }

    /*@Override
    public void load() {
        super.load();
    }*/

    @Override
    public void update(Tile tile) {
        UnitHiveSpawnerEntity entity = tile.entity();
        if(Vars.state.teams.get(tile.getTeam()).cores.isEmpty()) return;
        Tile core = Geometry.findClosest(tile.drawx(), tile.drawy(), Vars.state.teams.get(tile.getTeam()).cores);
        if(core == null) return;
        entity.spawnTimer += Timers.delta();
        if (entity.spawnTimer >= Mathf.random(minSpawnTimer, maxSpawnTimer) * 60f) {
            entity.spawnTimer = 0;
            spawn(tile, core);
        }
    }

    @Override
    public void drawLayer(Tile tile){
        if(Vars.state.teams.get(tile.getTeam()).cores.isEmpty()) return;
        Tile core = Geometry.findClosest(tile.drawx(), tile.drawy(), Vars.state.teams.get(tile.getTeam()).cores);
        if(core == null) return;

        Draw.color(Color.valueOf("3c0e0e"));
        Lines.stroke(1f + Mathf.absin(Timers.time(), 4f, 1f));
        Lines.line(tile.drawx(), tile.drawy(), core.drawx(), core.drawy());
        Draw.reset();
    }

    public TileEntity newEntity() {
        return new UnitHiveSpawnerEntity();
    }


    public static class UnitHiveSpawnerEntity extends TileEntity {
        public float spawnTimer;
        public int[] spawned;
    }
}
