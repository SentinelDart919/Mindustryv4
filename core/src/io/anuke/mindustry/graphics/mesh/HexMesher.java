package io.anuke.mindustry.graphics.mesh;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/** Defines color and height for a planet mesh. */
public interface HexMesher{
    default float getHeight(Vector3 position){
        return 0f;
    }

    default void getColor(Vector3 position, Color out){
        out.set(Color.WHITE);
    }

    default boolean skip(Vector3 position){
        return false;
    }
}
