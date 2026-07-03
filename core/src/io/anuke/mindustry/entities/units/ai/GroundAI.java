package io.anuke.mindustry.entities.units.ai;

import io.anuke.mindustry.entities.units.BaseUnit;

public class GroundAI extends AIController{
    public GroundAI(BaseUnit unit){
        super(unit);
    }

    @Override
    public void updateUnit(){
        unit.updateDefaultAI();
    }
}
