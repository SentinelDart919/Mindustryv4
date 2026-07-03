package io.anuke.mindustry.world.blocks;

import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import arc.math.geom.Geometry;
import arc.math.Mathf;

public interface Autotiler{
    class AutotilerHolder{
        public static final int[] blendresult = new int[5];
    }

    default int[] buildBlending(Tile tile, int rotation, boolean checkWorld){
        int[] blendresult = AutotilerHolder.blendresult;
        blendresult[0] = 0;
        blendresult[1] = blendresult[2] = 1;

        int num =
            (blends(tile, rotation, 2, checkWorld) && blends(tile, rotation, 1, checkWorld) && blends(tile, rotation, 3, checkWorld)) ? 0 :
            (blends(tile, rotation, 1, checkWorld) && blends(tile, rotation, 3, checkWorld)) ? 1 :
            (blends(tile, rotation, 1, checkWorld) && blends(tile, rotation, 2, checkWorld)) ? 2 :
            (blends(tile, rotation, 3, checkWorld) && blends(tile, rotation, 2, checkWorld)) ? 3 :
            blends(tile, rotation, 1, checkWorld) ? 4 :
            blends(tile, rotation, 3, checkWorld) ? 5 :
            -1;

        transformCase(num, blendresult);

        blendresult[3] = 0;
        for(int i = 0; i < 4; i++){
            if(blends(tile, rotation, i, checkWorld)){
                blendresult[3] |= (1 << i);
            }
        }

        blendresult[4] = 0;
        return blendresult;
    }

    default void transformCase(int num, int[] bits){
        switch(num){
            case 0:
                bits[0] = 3;
                break;
            case 1:
                bits[0] = 4;
                break;
            case 2:
                bits[0] = 2;
                break;
            case 3:
                bits[0] = 2;
                bits[2] = -1;
                break;
            case 4:
                bits[0] = 1;
                bits[2] = -1;
                break;
            case 5:
                bits[0] = 1;
                break;
        }
    }

    default boolean facing(int x, int y, int rotation, int x2, int y2){
        return x + Geometry.d4[rotation].x == x2 && y + Geometry.d4[rotation].y == y2;
    }

    default boolean blends(Tile tile, int rotation, int direction, boolean checkWorld){
        return checkWorld && blends(tile, rotation, direction);
    }

    default boolean blends(Tile tile, int rotation, int direction){
        Tile other = tile.getNearby(Mathf.mod(rotation - direction, 4));
        if(other != null) other = other.target();
        return other != null && other.getTeamID() == tile.getTeamID() &&
            blends(tile, rotation, other.x, other.y, other.getRotation(), other.block());
    }

    boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock);
}
