package io.anuke.mindustry.ui.fragments;

import arc.Core;
import io.anuke.mindustry.graphics.Palette;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;

public class LoadingFragment extends Fragment{
    private Table table;
    private TextButton button;

    @Override
    public void build(Group parent){
        parent.fill(t -> {
            t.background(Core.scene.getSkin().getDrawable("loadDim"));
            t.visible = false;
            t.touchable = Touchable.enabled;
            t.add().height(70f).row();

            t.image("white").growX().height(3f).pad(4f).growX().get().setColor(Palette.accent);
            t.row();
            t.add("$text.loading").name("namelabel").pad(10f);
            t.row();
            t.image("white").growX().height(3f).pad(4f).growX().get().setColor(Palette.accent);
            t.row();

            button = t.addButton("$text.cancel", () -> {}).pad(20).size(250f, 70f).visible(false).get();
            table = t;
        });
    }

    public void setButton(Runnable listener){
        button.visible = true;
        button.getListeners().removeIndex(button.getListeners().size - 1);
        button.clicked(listener);
    }

    public void show(){
        show("$text.loading");
    }

    public void show(String text){
        table.<Label>find("namelabel").setText(text);
        table.visible = true;
        table.toFront();
    }

    public void hide(){
        table.visible = false;
        button.visible = false;
    }
}
