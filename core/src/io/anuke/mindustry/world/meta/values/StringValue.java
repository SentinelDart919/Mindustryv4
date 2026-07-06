package io.anuke.mindustry.world.meta.values;

import io.anuke.mindustry.world.meta.StatValue;
import arc.scene.ui.layout.Table;
import arc.util.Strings;

public class StringValue implements StatValue{
    private final String value;

    public StringValue(String value, Object... args){
        this.value = Strings.format(value, args);
    }

    @Override
    public void display(Table table){
        table.add(value);
    }
}
