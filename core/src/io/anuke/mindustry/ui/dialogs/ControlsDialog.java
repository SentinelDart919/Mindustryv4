package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.util.Align;
import io.anuke.mindustry.graphics.Palette;

public class ControlsDialog extends FloatingDialog{

    public ControlsDialog(){
        super("$text.controls");
        setFillParent(true);
        title.setAlignment(Align.center);
        titleTable.row();
        titleTable.add(new Image((Drawable)Core.atlas.getDrawable("white")))
                .growX().height(3f).pad(4f).get().setColor(Palette.accent);
    }

    @Override
    public void addCloseButton(){
        buttons.button("$text.back", (Drawable)Core.atlas.getDrawable("icon-arrow-left"), 30f, this::hide).size(230f, 64f);

        keyDown(key -> {
            if(key == KeyCode.escape || key == KeyCode.back)
                hide();
        });
    }
}
