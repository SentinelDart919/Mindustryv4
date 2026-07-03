package io.anuke.mindustry.world;

import arc.graphics.Color;

public enum BarType{
    health(Color.SCARLET),
    inventory(Color.GREEN),
    power(Color.valueOf("fbeb67")),
    liquid(Color.royal),
    heat(Color.CORAL),
    production(Color.valueOf("f4ba6e"));

    public final Color color;

    BarType(Color color){
        this.color = color;
    }
}
