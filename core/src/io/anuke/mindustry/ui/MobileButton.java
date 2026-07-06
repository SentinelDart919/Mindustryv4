package io.anuke.mindustry.ui;

import arc.Core;
import arc.scene.style.Drawable;
import arc.util.Align;
import arc.scene.ui.ImageButton;

public class MobileButton extends ImageButton{

    public MobileButton(String icon, float isize, String text, Runnable listener){
        super((Drawable)Core.atlas.getDrawable(icon));
        resizeImage(isize);
        clicked(listener);
        row();
        add(text).growX().wrap().center().get().setAlignment(Align.center, Align.center);
    }
}

