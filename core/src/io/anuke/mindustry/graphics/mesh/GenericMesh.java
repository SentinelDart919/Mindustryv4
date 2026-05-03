package io.anuke.mindustry.graphics.mesh;

import com.badlogic.gdx.utils.Disposable;

public interface GenericMesh extends Disposable{
    @Override
    default void dispose(){}
}
