package io.anuke.mindustry.world.blocks.classic.distribution;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.PowerBlock;
import io.anuke.mindustry.world.meta.BlockGroup;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.scene.ui.ButtonGroup;
import io.anuke.ucore.scene.ui.ImageButton;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Mathf;
import io.anuke.mindustry.gen.Call;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Teleporter extends PowerBlock{
    public static final Color[] colorArray = {Color.ROYAL, Color.ORANGE, Color.SCARLET, Color.FOREST,
            Color.PURPLE, Color.GOLD, Color.PINK, Color.BLACK};
    public static final int colors = colorArray.length;

    private static final ObjectSet<Tile>[] teleporters = new ObjectSet[colors];
    private static byte lastColor = 0;

    static{
        for(int i = 0; i < colors; i++){
            teleporters[i] = new ObjectSet<>();
        }
    }

    private final Array<Tile> removal = new Array<>();
    private final Array<Tile> returns = new Array<>();

    protected float powerPerItem = 0.8f;

    public Teleporter(String name){
        super(name);
        update = true;
        solid = true;
        health = 80;
        hasItems = true;
        itemCapacity = 20;
        powerCapacity = 30f;
        instantTransfer = true;
        group = BlockGroup.transportation;
        configurable = true;
    }

    @Remote(targets = Loc.both, called = Loc.both, forward = true)
    public static void setTeleporterColor(Player player, Tile tile, byte color){
        TileEntity entity = tile.entity();
        if(entity instanceof TeleporterEntity){
            ((TeleporterEntity)entity).color = color;
        }
    }

    @Override
    public void playerPlaced(Tile tile){
        TeleporterEntity entity = tile.entity();
        entity.color = lastColor;
        Call.setTeleporterColor(null, tile, lastColor);
    }

    @Override
    public void draw(Tile tile){
        TeleporterEntity entity = tile.entity();

        super.draw(tile);

        Draw.color(colorArray[entity.color]);
        Draw.rect("blank", tile.worldx(), tile.worldy(), 2, 2);
        Draw.color(Color.WHITE);
        Draw.alpha(0.45f + Mathf.absin(Timers.time(), 7f, 0.26f));
        Draw.rect("teleporter-top", tile.worldx(), tile.worldy());
        Draw.reset();
    }

    @Override
    public void update(Tile tile){
        TeleporterEntity entity = tile.entity();

        teleporters[entity.color].add(tile);

        if(entity.items.total() > 0){
            tryDump(tile);
        }
    }

    @Override
    public void buildTable(Tile tile, Table table){
        TeleporterEntity entity = tile.entity();

        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        Table cont = new Table();
        cont.defaults().size(38);
        cont.margin(4);

        for(int i = 0; i < colors; i++){
            final int f = i;
            ImageButton button = cont.addImageButton("white", "clear-toggle", 24, () -> {
                lastColor = (byte)f;
                Call.setTeleporterColor(null, tile, (byte)f);
            }).group(group).get();
            button.getStyle().imageUpColor = colorArray[f];
            button.setChecked(entity.color == f);

            if(i % 4 == 3){
                cont.row();
            }
        }

        table.add(cont);
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        TeleporterEntity entity = tile.entity();

        //this is the receiving end (item arrived from another teleporter): store it, do not forward again
        if(source != null && source.block() instanceof Teleporter){
            entity.items.add(item, 1);
            return;
        }

        Array<Tile> links = findLinks(tile);

        if(links.size > 0){
            Tile target = links.random();
            target.block().handleItem(item, target, tile);
        }

        entity.power.amount -= powerPerItem;
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        TeleporterEntity entity = tile.entity();
        return !(source.block() instanceof Teleporter) && entity.power.amount >= powerPerItem && findLinks(tile).size > 0;
    }

    @Override
    public TileEntity newEntity(){
        return new TeleporterEntity();
    }

    Array<Tile> findLinks(Tile tile){
        TeleporterEntity entity = tile.entity();

        removal.clear();
        returns.clear();

        for(Tile other : teleporters[entity.color]){
            if(other != tile){
                if(other.block() instanceof Teleporter){
                    if(other.<TeleporterEntity>entity().color != entity.color){
                        removal.add(other);
                    }else if(other.entity().items.total() == 0){
                        returns.add(other);
                    }
                }else{
                    removal.add(other);
                }
            }
        }

        for(Tile remove : removal)
            teleporters[entity.color].remove(remove);

        return returns;
    }

    public static class TeleporterEntity extends TileEntity{
        public byte color = 0;

        @Override
        public void write(DataOutput stream) throws IOException{
            stream.writeByte(color);
        }

        @Override
        public void read(DataInput stream) throws IOException{
            color = stream.readByte();
        }

        @Override
        public Object config(){
            return color;
        }

        @Override
        public void configured(Object config){
            if(config instanceof Byte){
                color = (Byte)config;
            }
        }
    }
}