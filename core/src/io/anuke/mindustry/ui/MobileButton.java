package io.anuke.mindustry.ui;

import com.badlogic.gdx.utils.Align;
import io.anuke.ucore.scene.ui.ImageButton;
import io.anuke.ucore.scene.ui.Label;
import io.anuke.ucore.scene.ui.layout.Unit;

public class MobileButton extends ImageButton{

    public MobileButton(String icon, float isize, String text, Runnable listener){
        super(icon);
        resizeImage(isize);
        clicked(listener);
        row();
        Label label = add(text).growX().wrap().center().get();
        label.setAlignment(Align.center, Align.center);
        label.setFontScale(0.85f / Unit.dp.scl(1f));
    }
}
