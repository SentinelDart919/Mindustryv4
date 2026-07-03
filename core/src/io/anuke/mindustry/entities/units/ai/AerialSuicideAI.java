package io.anuke.mindustry.entities.units.ai;

import io.anuke.mindustry.entities.units.BaseUnit;
import io.anuke.mindustry.entities.units.UnitCommand;

public class AerialSuicideAI extends FlyingAI{
    public AerialSuicideAI(BaseUnit unit){
        super(unit);
    }

    @Override
    public void updateUnit(){
        if(unit.isCommanded() && unit.getCommand() != UnitCommand.attack){
            unit.onCommand(UnitCommand.attack);
        }
        unit.updateDefaultAI();
    }
}
