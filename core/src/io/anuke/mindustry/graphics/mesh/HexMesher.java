package io.anuke.mindustry.graphics.mesh;

import arc.graphics.Color;
import arc.math.geom.Vec3;
import arc.math.geom.Vector3;
import arc.math.geom.PerspectiveCamera;

/** Defines color and height for a planet mesh. */
public interface HexMesher{
    default float getHeight(Vector3 position){
        return 0f;
    }

    default void getColor(Vector3 position, Color out){
        out.set(Color.white);
    }

    default boolean skip(Vector3 position){
        return false;
    }
}

