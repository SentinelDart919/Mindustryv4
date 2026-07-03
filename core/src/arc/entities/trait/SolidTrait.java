package arc.entities.trait;

import arc.entities.EntityQuery;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.util.QuadTree.QuadTreeObject;

public interface SolidTrait extends QuadTreeObject, PosTrait, MoveTrait, VelocityTrait, Entity{
    void getHitbox(Rect rectangle);
    void getHitboxTile(Rect rectangle);
    Vec2 lastPosition();

    default float getDeltaX(){
        return getX() - lastPosition().x;
    }

    default float getDeltaY(){
        return getY() - lastPosition().y;
    }

    default boolean movable(){ return false; }
    default boolean collides(SolidTrait other){ return true; }
    default void collision(SolidTrait other, float x, float y){}

    default void move(float x, float y){
        EntityQuery.collisions().move(this, x, y);
    }
}