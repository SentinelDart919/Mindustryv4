package io.anuke.mindustry.world.blocks;

import arc.struct.Seq;
import io.anuke.mindustry.type.Item;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.g2d.Draw;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.*;

public interface SelectionTrait{

    default void buildItemTable(Table table, Prov<Item> holder, Cons<Item> Cons){
        buildItemTable(table, false, holder, Cons);
    }

    default void buildItemTable(Table table, boolean nullItem, Prov<Item> holder, Cons<Item> Cons){

        Seq<Item> items = content.items();

        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        Table cont = new Table();
        cont.defaults().size(38);

        int i = 0;

        if(nullItem){
            ImageButton button = cont.addImageButton("white", "clear-toggle", 24, () -> Cons.get(null)).group(group).get();
            button.getStyle().imageUp = new TextureRegionDrawable(Core.atlas.find("icon-nullitem"));
            button.setChecked(holder.get() == null);

            i ++;
        }

        for(Item item : items){
            if(!control.unlocks.isUnlocked(item)) continue;

            ImageButton button = cont.addImageButton("white", "clear-toggle", 24, () -> Cons.get(item))
                    .group(group).get();
            button.getStyle().imageUp = new TextureRegionDrawable(item.region);
            button.setChecked(holder.get() == item);

            if(i++ % 4 == 3){
                cont.row();
            }
        }

        table.add(cont);
    }
}
