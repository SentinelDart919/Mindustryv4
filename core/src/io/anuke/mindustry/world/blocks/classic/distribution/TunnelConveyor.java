package io.anuke.mindustry.world.blocks.classic.distribution;

import com.badlogic.gdx.utils.NumberUtils;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.BarType;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.distribution.Conveyor;
import io.anuke.mindustry.world.blocks.distribution.Junction;
import io.anuke.mindustry.world.blocks.distribution.Router;
import io.anuke.mindustry.world.meta.BlockBar;
import io.anuke.mindustry.world.meta.BlockGroup;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.Bits;

import static io.anuke.mindustry.Vars.content;

public class TunnelConveyor extends Block{
    protected int maxdist = 3;
    protected float speed = 53;
    protected int capacity = 32;

    public TunnelConveyor(String name){
        super(name);
        rotate = true;
        update = true;
        solid = true;
        health = 70;
        instantTransfer = true;
        group = BlockGroup.transportation;
        hasItems = true;
        itemCapacity = capacity;
        bars.add(new BlockBar(BarType.inventory, true, tile -> (float)tile.<TunnelEntity>entity().index / capacity));
    }

    @Override
    public boolean canReplace(Block other){
        return other instanceof Conveyor || other instanceof Router || other instanceof Junction;
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        TunnelEntity entity = tile.entity();

        if(entity.index >= entity.buffer.length) return;

        entity.buffer[entity.index++] = Bits.packLong(NumberUtils.floatToIntBits(Timers.time()), item.id);
    }

    @Override
    public void update(Tile tile){
        TunnelEntity entity = tile.entity();

        if(entity.index > 0){
            long l = entity.buffer[0];
            float time = NumberUtils.intBitsToFloat(Bits.getLeftInt(l));

            if(Timers.time() >= time + speed || Timers.time() < time){

                Item item = content.item(Bits.getRightInt(l));

                Tile tunnel = getDestTunnel(tile, item);
                if(tunnel == null) return;
                Tile target = tunnel.getNearby(tunnel.getRotation());
                if(target == null) return;

                if(!target.block().acceptItem(item, target, tunnel)) return;

                target.block().handleItem(item, target, tunnel);
                System.arraycopy(entity.buffer, 1, entity.buffer, 0, entity.index - 1);
                entity.index--;
            }
        }
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        TunnelEntity entity = tile.entity();
        int rot = source.relativeTo(tile.x, tile.y);
        if(rot != (tile.getRotation() + 2) % 4) return false;
        return entity.index < entity.buffer.length - 1;
    }

    @Override
    public TileEntity newEntity(){
        return new TunnelEntity();
    }

    Tile getDestTunnel(Tile tile, Item item){
        Tile dest = tile;
        int rel = (tile.getRotation() + 2) % 4;
        for(int i = 0; i < maxdist; i++){
            if(dest == null) return null;
            dest = dest.getNearby(rel);
            if(dest != null && dest.block() instanceof TunnelConveyor && dest.getRotation() == rel
                    && dest.getNearby(rel) != null
                    && dest.getNearby(rel).block().acceptItem(item, dest.getNearby(rel), dest)){
                return dest;
            }
        }
        return null;
    }

    class TunnelEntity extends TileEntity{
        long[] buffer = new long[capacity];
        int index;
    }
}