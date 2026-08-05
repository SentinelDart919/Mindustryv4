package io.anuke.mindustry.entities;

import com.badlogic.gdx.math.Vector2;

/**Represents a single leg of a {@link io.anuke.mindustry.entities.units.LegsUnit}.*/
public class Leg{
    /**Position of the leg joint. Joint is positioned between the base and the foot.*/
    public final Vector2 joint = new Vector2(), base = new Vector2();
    /**Group of legs this leg moves with.*/
    public int group;
    /**Whether this leg is currently moving.*/
    public boolean moving;
    /**How far along the movement cycle this leg is (0-1).*/
    public float stage;
}
