package io.anuke.mindustry.world.blocks.distribution;

import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Edges;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.util.Geometry;
import io.anuke.ucore.util.Mathf;
import static io.anuke.mindustry.Vars.*;

public class ArmoredConveyor extends Conveyor{
    public ArmoredConveyor(String name){
        super(name);
        noSideBlend = true;
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return (otherblock.outputsItems() && blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock)) ||
            (facing(tile.x, tile.y, rotation, otherx, othery) && otherblock.hasItems);
    }

    public boolean blendsArmored(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return (tile.x + Geometry.d4[rotation].x == otherx && tile.y + Geometry.d4[rotation].y == othery)
            || ((!otherblock.rotate && Edges.getFacingEdge(world.tile(otherx, othery), tile) != null
            && Edges.getFacingEdge(world.tile(otherx, othery), tile).relativeTo(tile.x, tile.y) == rotation)
            || (otherblock instanceof Conveyor && otherblock.rotate
            && otherx + Geometry.d4[otherrot].x == tile.x && othery + Geometry.d4[otherrot].y == tile.y));
    }

    @Override
    public boolean blends(Tile tile, int direction){
        Tile other = tile.getNearby(Mathf.mod(tile.getRotation() - direction, 4));
        if(other != null && other.block() instanceof Conveyor && ((!other.block().rotate || other.getNearby(other.getRotation()) == tile))) other = other.target();
        return other != null && other.block().outputsItems() && other.block() instanceof Conveyor && (!other.block().rotate || other.getNearby(other.getRotation()) == tile);
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return super.acceptItem(item, tile, source) &&
            (source.block() instanceof Conveyor || Edges.getFacingEdge(source, tile).relativeTo(tile.x, tile.y) == tile.getRotation());
    }
}
