package io.anuke.mindustry.entities.units;

import arc.util.Bundles;
import arc.util.Strings;

public enum UnitCommand{
    attack, retreat, patrol;

    private final String localized;

    UnitCommand(){
        localized = Bundles.get("command." + name());
    }

    public String localized(){
        return localized;
    }
}
