package io.anuke.mindustry.world.meta;

/**A specific category for a stat.*/
public enum StatCategory{
    general,
    power,
    liquids,
    items,
    crafting,
    shooting,
    optional;

    public String localized(){
        return io.anuke.ucore.util.Bundles.get("text.category." + name());
    }
}
