package io.anuke.mindustry.game;

import io.anuke.ucore.util.Bundles;

public enum Difficulty{
    training(3f, 3f, 0.5f),
    easy(1.4f, 1.5f,0.75f),
    normal(1f, 1f, 1f),
    hard(0.5f, 0.75f, 1.5f),
    insane(0.25f, 0.5f, 2f);

    /**Multiplier of the time between waves.*/
    public final float timeScaling;
    /**Multiplier of spawner grace period.*/
    public final float spawnerScaling;
    /**Multiplier of unit spawning period.*/
    public final float UnitAmountScaling;

    private String value;

    Difficulty(float timeScaling, float spawnerScaling, float UnitAmountScaling){
        this.timeScaling = timeScaling;
        this.spawnerScaling = spawnerScaling;
        this.UnitAmountScaling = UnitAmountScaling;
    }

    @Override
    public String toString(){
        if(value == null){
            value = Bundles.get("setting.difficulty." + name());
        }
        return value;
    }
}
