package io.anuke.mindustry.ui.dialogs;

import io.anuke.mindustry.game.Schematic;
import io.anuke.mindustry.input.PlaceMode;
import io.anuke.ucore.scene.ui.ScrollPane;
import io.anuke.ucore.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.*;

public class SchematicsDialog extends FloatingDialog {
    public SchematicsDialog() {
        super("Schematics");
        addCloseButton();
        shown(this::rebuild);
    }

    void rebuild() {
        content().clear();
        Table table = new Table();
        table.margin(10);
        ScrollPane pane = new ScrollPane(table);
        
        for (Schematic s : schematics.all()) {
            table.addButton(s.name(), "clear", () -> {
                control.input(0).schematic = s.copy();
                control.input(0).mode = PlaceMode.schematic;
                hide();
            }).size(400, 50).pad(4);
            
            table.addImageButton("icon-trash", "clear", 40, () -> {
                ui.showConfirm("Delete Schematic", "Are you sure you want to delete '" + s.name() + "'?", () -> {
                    schematics.remove(s);
                    rebuild();
                });
            }).size(50).pad(4).row();
        }
        
        content().add(pane);
    }
}
