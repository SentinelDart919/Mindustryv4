package arc.entities.trait;

import arc.math.geom.Position;
import arc.math.Mathf;
import arc.math.geom.Vec2;

public interface PosTrait extends Position{
    float getX();
    float getY();

    default float angleTo(PosTrait other){
        return Mathf.atan2(other.getX() - getX(), other.getY() - getY());
    }

    default float angleTo(PosTrait other, float yoffset){
        return Mathf.atan2(other.getX() - getX(), other.getY() - (getY() + yoffset));
    }

    default float angleTo(float ox, float oy){
        return Mathf.atan2(ox - getX(), oy - getY());
    }

    default float angleTo(PosTrait other, float xoffset, float yoffset){
        return Mathf.atan2(other.getX() - (getX() + xoffset), other.getY() - (getY() + yoffset));
    }

    default float dst(PosTrait other){
        return Mathf.dst(getX(), getY(), other.getX(), other.getY());
    }

    default float dst(float ox, float oy){
        return Mathf.dst(getX(), getY(), ox, oy);
    }
}