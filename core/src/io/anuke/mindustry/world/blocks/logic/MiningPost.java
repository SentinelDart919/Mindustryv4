package io.anuke.mindustry.world.blocks.logic;

import arc.struct.IntSeq;
import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.meta.BlockFlag;
import arc.util.Time;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import java.util.EnumSet;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MiningPost extends Block {
    public UnitType postDrone;
    public int maxDrones = 4;
    public Item[] ItemOptions;

    public MiningPost(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        itemCapacity = 100;
        configurable = true;
        flags = EnumSet.of(BlockFlag.dropPoint);
    }

    @Override
    public void update(Tile tile) {
        MiningPostEntity entity = tile.entity();

        if (entity.selectedItem != null && entity.droneIDs.size < maxDrones && (entity.spawnTimer += Timers.delta()) >= 60f * 5) {
            BaseUnit unit = postDrone.create(tile.getTeam());
            unit.setSpawner(tile);
            unit.set(tile.worldx(), tile.worldy());
            unit.add();
            entity.droneIDs.add(unit.id);
            entity.spawnTimer = 0;
        }

        if (entity.items.total() > 0) {
            tryDump(tile);
        }
    }

    @Override
    public void unitRemoved(Tile tile, Unit unit) {
        MiningPostEntity entity = tile.entity();
        entity.droneIDs.remove(unit.id);
    }

    @Remote(targets = Loc.both, called = Loc.both, forward = true)
    public static void setMiningPostItem(Player player, Tile tile, Item item) {
        MiningPostEntity entity = tile.entity();
        if (entity != null) entity.selectedItem = item;
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source) {
        return hasItems && tile.entity.items.get(item) < getMaximumAccepted(tile, item);
    }

    @Override
    public void buildTable(Tile tile, Table table) {
        MiningPostEntity entity = tile.entity();
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        table.add("Mining Options: ").row(); // first time I use text for tables, just for testing

        table.table(t -> {
            int i = 0;
            if (ItemOptions != null) {
                for (Item item : ItemOptions) {
                    ImageButton button = t.addImageButton("white", "clear-toggle", 24, () -> {
                        Call.setMiningPostItem(null, tile, item);
                    }).size(38).group(group).get();
                    button.getStyle().imageUp = new TextureRegionDrawable(item.getContentIcon());
                    button.setChecked(entity.selectedItem == item);

                    if (++i % 4 == 0) t.row();
                }
            }

            // null option
            ImageButton naButton = t.addImageButton("icon-cancel", "clear-toggle", 24, () -> {
                Call.setMiningPostItem(null, tile, null);
            }).size(38).group(group).get();
            naButton.setChecked(entity.selectedItem == null);
        }).row();
    }

    @Override
    public TileEntity newEntity() {
        return new MiningPostEntity();
    }

    public static class MiningPostEntity extends TileEntity {
        public Item selectedItem;
        public IntSeq droneIDs = new IntSeq();
        public float spawnTimer;

        @Override
        public void write(DataOutput stream) throws IOException {
            super.write(stream);
            stream.writeShort(selectedItem == null ? -1 : selectedItem.id);
        }

        @Override
        public void read(DataInput stream) throws IOException {
            super.read(stream);
            int id = stream.readShort();
            selectedItem = id == -1 ? null : Vars.content.item(id);
        }

        @Override
        public void writeConfig(DataOutput stream) throws IOException {
            stream.writeShort(selectedItem == null ? -1 : selectedItem.id);
        }

        @Override
        public void readConfig(DataInput stream) throws IOException {
            int id = stream.readShort();
            selectedItem = id == -1 ? null : Vars.content.item(id);
        }
    }
}


