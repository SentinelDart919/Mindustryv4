package io.anuke.mindustry.entities.traits;

import io.anuke.mindustry.entities.Timer;
import io.anuke.mindustry.type.Weapon;
import arc.entities.trait.VelocityTrait;

public interface ShooterTrait extends VelocityTrait, TeamTrait, InventoryTrait{

    Timer getTimer();

    int getShootTimer(boolean left);

    Weapon getWeapon();
}
