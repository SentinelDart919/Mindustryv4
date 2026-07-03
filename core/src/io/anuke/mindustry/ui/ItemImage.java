package io.anuke.mindustry.ui;

import arc.graphics.g2d.TextureRegion;
import io.anuke.mindustry.type.ItemStack;
import arc.func.Prov;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;

public class ItemImage extends Stack{

    public ItemImage(TextureRegion region, Prov<CharSequence> text){
        Table t = new Table().left().bottom();
        t.label(text);

        add(new Image(region));
        add(t);
    }

    public ItemImage(ItemStack stack){
        add(new Image(stack.item.region));

        if(stack.amount != 0){
            Table t = new Table().left().bottom();
            t.add(stack.amount + "");
            add(t);
        }
    }
}
