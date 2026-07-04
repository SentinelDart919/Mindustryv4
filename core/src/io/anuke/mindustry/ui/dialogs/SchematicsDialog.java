package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import io.anuke.mindustry.game.Schematic;
import io.anuke.mindustry.input.PlaceMode;

import static io.anuke.mindustry.Vars.*;

public class SchematicsDialog extends FloatingDialog {
    public SchematicsDialog() {
        super("Schematics");
        addCloseButton();
        shown(this::rebuild);
    }

    void rebuild() {
        cont.clear();
        Table table = new Table();
        table.margin(10);
        ScrollPane pane = new ScrollPane(table);
        
        for (Schematic s : schematics.all()) {
            table.button(s.name(), () -> {
                control.input(0).schematic = s.copy();
                control.input(0).mode = PlaceMode.schematic;
                hide();
            }).size(400, 50).pad(4);
            
            table.button((Drawable)Core.atlas.getDrawable("icon-trash"), 40, () -> {
                ui.showConfirm("Delete Schematic", "Are you sure you want to delete '" + s.name() + "'?", () -> {
                    schematics.remove(s);
                    rebuild();
                });
            }).size(50).pad(4).row();
        }
        
        cont.add(pane);
    }
}
