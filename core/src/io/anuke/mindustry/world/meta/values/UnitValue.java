package io.anuke.mindustry.world.meta.values;

import io.anuke.mindustry.entities.units.UnitType;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.world.meta.StatValue;
import io.anuke.ucore.scene.ui.Image;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Bundles;
import io.anuke.ucore.util.Strings;

/**Displays the unit(s) a factory crafts, along with their required resources, craft time and max unit count.
 * Each unit is shown on its own line, with the required items wrapping to a new line every 3 items.*/
public class UnitValue implements StatValue{
    private static final int itemsPerRow = 3;

    private final UnitType[] units;
    private final ItemStack[][] requirements;
    private final float[] times;
    private final int[] maxSpawns;

    public UnitValue(UnitType unit, ItemStack[] requirements, float time){
        this(new UnitType[]{unit}, new ItemStack[][]{requirements}, new float[]{time}, null);
    }

    public UnitValue(UnitType[] units, ItemStack[][] requirements, float[] times, int[] maxSpawns){
        this.units = units;
        this.requirements = requirements;
        this.times = times;
        this.maxSpawns = maxSpawns;
    }

    @Override
    public void display(Table table){
        //move the value onto its own row so the stat label doesn't displace the first unit
        table.row();

        for(int i = 0; i < units.length; i++){
            final UnitType unit = units[i];
            final ItemStack[] stacks = requirements[i];
            final float time = times[i];
            final int max = maxSpawns == null ? -1 : maxSpawns[i];

            table.table(recipe -> {
                recipe.left();
                recipe.addImage(unit.getContentIcon()).size(8 * 4);
                recipe.add("[accent]" + unit.localizedName()).padLeft(4);
                recipe.add("[LIGHT_GRAY]->[]").padLeft(8);

                Table items = new Table();
                items.left();
                for(int j = 0; j < stacks.length; j++){
                    addItem(items, stacks[j]);
                    if(j % itemsPerRow == itemsPerRow - 1 && j < stacks.length - 1){
                        items.row();
                    }
                }
                recipe.add(items).padLeft(8);

                recipe.add("[LIGHT_GRAY]|[]").padLeft(8);
                recipe.add(Strings.toFixed(time / 60f, 1) + "s").padLeft(4);
                if(max >= 0){
                    recipe.add("[LIGHT_GRAY]|[]").padLeft(8);
                    recipe.add("[accent]" + Bundles.get("text.blocks.maxunits") + ":[] " + max).padLeft(4);
                }
            }).left().padTop(3f);

            if(i < units.length - 1){
                table.row();
            }
        }
    }

    private void addItem(Table table, ItemStack stack){
        table.table(item -> {
            item.left();
            item.add(new Image(stack.item.region)).size(8 * 3);
            item.add(stack.item.localizedName()).padLeft(3);
            item.add("[LIGHT_GRAY]x" + stack.amount + "[]").padLeft(4);
        }).padRight(8);
    }
}
