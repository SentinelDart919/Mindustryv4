package io.anuke.mindustry.entities.units.ai;

import io.anuke.mindustry.entities.units.BaseUnit;

public abstract class AIController{
    protected final BaseUnit unit;

    public AIController(BaseUnit unit){
        this.unit = unit;
    }

    public abstract void updateUnit();
}
