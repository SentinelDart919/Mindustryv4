package io.anuke.mindustry.world.blocks.logic;

import com.badlogic.gdx.utils.IntArray;
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
import io.anuke.mindustry.world.meta.BlockStat;
import io.anuke.mindustry.world.meta.StatUnit;
import io.anuke.mindustry.world.meta.values.ItemListValue;
import io.anuke.mindustry.world.meta.values.UnitListValue;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.scene.style.TextureRegionDrawable;
import io.anuke.ucore.scene.ui.ButtonGroup;
import io.anuke.ucore.scene.ui.ImageButton;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.EnumSet;

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
            useContent(tile, postDrone);
        }

        if (entity.items.total() > 0) {
            tryDump(tile);
        }
    }

    @Override
    public void setStats() {
        super.setStats();

        if (ItemOptions != null && ItemOptions.length > 0) {
            stats.add(BlockStat.mineItems, new ItemListValue(ItemOptions));
        }

        if (postDrone != null) {
            stats.add(BlockStat.spawnUnit, new UnitListValue(postDrone));
        }

        stats.add(BlockStat.maxUnits, maxDrones, StatUnit.none);
    }

    @Override
    public void unitRemoved(Tile tile, Unit unit) {
        MiningPostEntity entity = tile.entity();
        if (entity != null) {
            entity.droneIDs.removeValue(unit.id);
        }
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
        table.add("$text.logic.mining.options").row(); // first time I use text for tables, just for testing

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
        public IntArray droneIDs = new IntArray();
        public float spawnTimer;

        @Override
        public void write(DataOutput stream) throws IOException {
            super.write(stream);
            stream.writeShort(selectedItem == null ? -1 : selectedItem.id);
            stream.writeShort(droneIDs.size);
            for(int i = 0; i < droneIDs.size; i++){
                stream.writeInt(droneIDs.get(i));
            }
        }

        @Override
        public void read(DataInput stream) throws IOException {
            super.read(stream);
            int id = stream.readShort();
            selectedItem = id == -1 ? null : Vars.content.item(id);
            int amount = stream.readShort();
            droneIDs.clear();
            for(int i = 0; i < amount; i++){
                droneIDs.add(stream.readInt());
            }
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

        @Override
        public Object config(){
            return selectedItem;
        }

        @Override
        public void configured(Object config){
            if(config instanceof Item){
                selectedItem = (Item)config;
            }else{
                selectedItem = null;
            }
        }
    }
}
