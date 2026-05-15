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

public class LogicBlock extends Block {

    public LogicBlock(String name) {
        super(name);
        update = true;
        solid = true;
        configurable = true;
    }

    @Remote(targets = Loc.both, called = Loc.both, forward = true)
    public static void setLogicTarget(Player player, Tile tile, int targetPos) {
        LogicEntity entity = tile.entity();
        if (entity != null) entity.targetPos = targetPos;
    }

    @Override
    public boolean onConfigureTileTapped(Tile tile, Tile other) {
        if (tile != other && other.block() instanceof LogicBlock) {
            setLogicTarget(null, tile, other.id());
            return false;
        }
        return super.onConfigureTileTapped(tile, other);
    }

    public static class LogicEntity extends TileEntity {
        public int targetPos = -1;

        @Override
        public void write(DataOutput stream) throws IOException {
            super.write(stream);
            stream.writeInt(targetPos);
        }

        @Override
        public void read(DataInput stream) throws IOException {
            super.read(stream);
            targetPos = stream.readInt();
        }

        @Override
        public void writeConfig(DataOutput stream) throws IOException {
            stream.writeInt(targetPos);
        }

        @Override
        public void readConfig(DataInput stream) throws IOException {
            targetPos = stream.readInt();
        }
    }
}
