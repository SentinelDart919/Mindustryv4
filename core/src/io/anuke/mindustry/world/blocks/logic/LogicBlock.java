package io.anuke.mindustry.world.blocks.logic;

import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static io.anuke.mindustry.Vars.*;

public class LogicBlock extends Block {

    public LogicBlock(String name) {
        super(name);
        update = true;
        solid = true;
        configurable = true;
    }

    @Remote(targets = Loc.both, called = Loc.both, forward = true)
    public static void setLogicTarget(Player player, Tile tile, long targetPos) {
        LogicEntity entity = tile.entity();
        if (entity != null) entity.targetPos = targetPos;
    }

    @Override
    public boolean onConfigureTileTapped(Tile tile, Tile other) {
        Tile target = other.target();
        if (tile != target && target.block() instanceof LogicBlock) {
            setLogicTarget(null, tile, target.packedPosition());
            return false;
        }
        return super.onConfigureTileTapped(tile, other);
    }

    public static class LogicEntity extends TileEntity {
        public long targetPos = -1L;

        @Override
        public void write(DataOutput stream) throws IOException {
            super.write(stream);
            stream.writeLong(targetPos);
        }

        @Override
        public void read(DataInput stream) throws IOException {
            super.read(stream);
            targetPos = stream.readLong();
        }

        @Override
        public void writeConfig(DataOutput stream) throws IOException {
            stream.writeLong(targetPos);
        }

        @Override
        public void readConfig(DataInput stream) throws IOException {
            targetPos = stream.readLong();
        }

        @Override
        public Object config(){
            if(targetPos == -1L) return null;
            Tile other = world.tile(targetPos);
            if(other == null) return null;
            int dx = other.x - tile.x;
            int dy = other.y - tile.y;
            return (dx << 16) | (dy & 0xFFFF);
        }

        @Override
        public void configured(Object config){
            if(config instanceof Integer){
                int rel = (Integer)config;
                int dx = rel >> 16;
                int dy = (short)(rel & 0xFFFF);
                Tile other = world.tile(tile.x + dx, tile.y + dy);
                if(other != null && other.block() instanceof LogicBlock){
                    targetPos = other.packedPosition();
                }
            }
        }
    }
}
