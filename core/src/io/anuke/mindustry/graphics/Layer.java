package io.anuke.mindustry.graphics;

public enum Layer{
    /**
     * Layer for things drawn UNDER blocks.
     */
    back,
    /**
     * Base block layer.
     */
    block,
    /**
     * for placement
     */
    placement,
    /**
     * First overlay. Stuff like conveyor items.
     */
    overlay,
    /**
     * "High" blocks, like turrets.
     */
    turret,
    /**
     * Power lasers.
     */
    power,
    /**
     * Extra lasers, like healing turrets.
     */
    laser
}
