package io.anuke.mindustry.entities.units.ai;

import io.anuke.mindustry.entities.units.BaseUnit;

public class FlyingAI extends AIController{
    public FlyingAI(BaseUnit unit){
        super(unit);
    }

    @Override
    public void updateUnit(){
        unit.updateDefaultAI();
    }
}
