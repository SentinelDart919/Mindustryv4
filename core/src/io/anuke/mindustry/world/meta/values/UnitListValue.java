package io.anuke.mindustry.world.meta.values;

import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.world.meta.StatValue;
import io.anuke.ucore.scene.ui.layout.Table;

/**Displays the unit type(s) a block spawns, showing their icon and name.*/
public class UnitListValue implements StatValue{
    private final UnitType[] units;

    public UnitListValue(UnitType... units){
        this.units = units;
    }

    @Override
    public void display(Table table){
        for(UnitType unit : units){
            table.table(t -> {
                t.left();
                t.addImage(unit.getContentIcon()).size(8 * 4);
                t.add("[accent]" + unit.localizedName() + "[]").padLeft(4);
            }).padTop(3f);
            table.row();
        }
    }
}
