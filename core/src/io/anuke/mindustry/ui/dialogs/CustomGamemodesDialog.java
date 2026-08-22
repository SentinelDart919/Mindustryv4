package io.anuke.mindustry.ui.dialogs;

import io.anuke.mindustry.ui.MenuButton;
import io.anuke.ucore.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.ui;

public class CustomGamemodesDialog extends FloatingDialog{

    public CustomGamemodesDialog(){
        super("$text.customgamemodes");
        addCloseButton();
        setup();
    }

    void setup(){
        content().clear();

        Table table = new Table();
        table.defaults().size(280f, 66f).pad(5f);

        table.add(new MenuButton("icon-play-2", "$mode.openworld.name", () -> {
            hide();
            ui.openWorldStart.show();
        }));

        content().add(table);
    }
}
