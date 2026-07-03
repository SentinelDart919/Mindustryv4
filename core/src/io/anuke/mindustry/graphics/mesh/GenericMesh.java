package io.anuke.mindustry.graphics.mesh;

import arc.util.Disposable;

public interface GenericMesh extends Disposable{
    @Override
    default void dispose(){}
}
