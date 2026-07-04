package io.anuke.mindustry.world;

import arc.graphics.Color;

public enum BarType{
    health(Color.scarlet),
    inventory(Color.green),
    power(Color.valueOf("fbeb67")),
    liquid(Color.royal),
    heat(Color.coral),
    production(Color.valueOf("f4ba6e"));

    public final Color color;

    BarType(Color color){
        this.color = color;
    }
}
