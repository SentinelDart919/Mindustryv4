package io.anuke.mindustry.world.meta.values;

import arc.struct.Seq;
import io.anuke.mindustry.type.Liquid;
import io.anuke.mindustry.ui.LiquidDisplay;
import io.anuke.mindustry.world.meta.StatValue;
import arc.func.Boolf;
import arc.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.content;

public class LiquidFilterValue implements StatValue{
    private final Boolf<Liquid> filter;

    public LiquidFilterValue(Boolf<Liquid> filter){
        this.filter = filter;
    }

    @Override
    public void display(Table table){
        Seq<Liquid> list = new Seq<>();

        for(Liquid item : content.liquids()){
            if(!item.isHidden() && filter.get(item)) list.add(item);
        }

        for(int i = 0; i < list.size; i++){
            table.add(new LiquidDisplay(list.get(i))).padRight(5);

            if(i != list.size - 1){
                table.add("/");
            }
        }
    }
}
