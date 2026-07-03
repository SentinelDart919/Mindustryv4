package io.anuke.mindustry.game;

import arc.Core;
import arc.graphics.Color;
import arc.util.Strings;

public enum Team{
    none(Color.valueOf("4d4e58")),
    blue(Color.royal),
    red(Color.valueOf("e84737")),
    green(Color.valueOf("1dc645")),
    purple(Color.valueOf("ba5bd9")),
    orange(Color.valueOf("e8c66a")),
    themass(Color.valueOf("320303"));

    public final static Team[] all = values();
    public final Color color;
    public final int intColor;

    Team(Color color){
        this.color = color;
        intColor = color.rgba();
    }

    public String localized(){
        return Core.bundle.get("team." + name() + ".name");
    }
}
