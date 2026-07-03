package io.anuke.mindustry.world.blocks.distribution;

import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Tile;
import arc.math.Mathf;
public class ArmoredConveyor extends Conveyor{
    public ArmoredConveyor(String name){
        super(name);
        noSideBlend = true;
    }
    @Override
    public boolean blends(Tile tile, int direction){
        Tile other = tile.getNearby(Mathf.mod(tile.getRotation() - direction, 4));
        if(other != null && other.block() instanceof Conveyor && ((!other.block().rotate || other.getNearby(other.getRotation()) == tile))) other = other.target();

        return other != null && other.block().outputsItems() && other.block() instanceof Conveyor && (!other.block().rotate || other.getNearby(other.getRotation()) == tile);
    }
@Override
public boolean acceptItem(Item item, Tile tile, Tile source){
    int direction = source == null ? 0 : Math.abs(source.relativeTo(tile.x, tile.y) - tile.getRotation());
    float minitem = tile.<ConveyorEntity>entity().minitem;
    return (((direction == 0) && minitem > itemSpace) ||
            ((direction % 2 == 1) && minitem > 0.52f)) && (source == null || !(source.block().rotate && (source.getRotation() + 2) % 4 == tile.getRotation() ))
            && (source.block() instanceof Conveyor || source.block() instanceof Junction || source.block().outputsItems() && tile.getNearby(tile.getRotation()) == source.getNearby(source.getRotation()))
            && (!source.block().rotate || source.getNearby(source.getRotation()) == tile);
    }
}


