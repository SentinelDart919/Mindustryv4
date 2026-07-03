package io.anuke.mindustry.world.meta.values;

import arc.struct.Seq;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.ui.ItemDisplay;
import io.anuke.mindustry.world.meta.StatValue;
import arc.func.Boolf;
import arc.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.content;

public class ItemFilterValue implements StatValue{
    private final Boolf<Item> filter;

    public ItemFilterValue(Boolf<Item> filter){
        this.filter = filter;
    }

    @Override
    public void display(Table table){
        Seq<Item> list = new Seq<>();

        for(Item item : content.items()){
            if(filter.get(item)) list.add(item);
        }

        for(int i = 0; i < list.size; i++){
            Item item = list.get(i);

            table.add(new ItemDisplay(item)).padRight(5);

            if(i != list.size - 1){
                table.add("/");
            }
        }
    }
}
