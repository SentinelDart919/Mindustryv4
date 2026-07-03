package io.anuke.mindustry.entities.traits;

import arc.entities.trait.DamageTrait;
import arc.entities.trait.Entity;

public interface AbsorbTrait extends Entity, TeamTrait, DamageTrait{
    void absorb();

    default boolean canBeAbsorbed(){
        return true;
    }

    default float getShieldDamage(){
        return getDamage();
    }
}
