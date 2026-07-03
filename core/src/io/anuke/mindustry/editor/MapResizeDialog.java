package io.anuke.mindustry.editor;

import io.anuke.mindustry.maps.MapTileData;
import io.anuke.mindustry.ui.dialogs.FloatingDialog;
import arc.func.Cons2;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.math.Mathf;

public class MapResizeDialog extends FloatingDialog{
    int[] validMapSizes = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
    int width, height;

    public MapResizeDialog(MapEditor editor, Cons2<Integer, Integer> cons){
        super("$text.editor.resizemap");
        shown(() -> {
            content().clear();
            MapTileData data = editor.getMap();
            width = data.width();
            height = data.height();

            Table table = new Table();

            for(boolean w : Mathf.booleans){
                int curr = w ? data.width() : data.height();
                int idx = 0;
                for(int i = 0; i < validMapSizes.length; i++){
                    if(validMapSizes[i] == curr) idx = i;
                }

                table.add(w ? "$text.width" : "$text.height").padRight(8f);
                ButtonGroup<TextButton> group = new ButtonGroup<>();
                for(int i = 0; i < validMapSizes.length; i++){
                    int size = validMapSizes[i];
                    TextButton button = new TextButton(size + "", "toggle");
                    button.clicked(() -> {
                        if(w)
                            width = size;
                        else
                            height = size;
                    });
                    group.add(button);
                    if(i == idx) button.setChecked(true);
                    table.add(button).size(100f, 54f).pad(2f);
                }

                table.row();
            }
            content().row();
            content().add(table);

        });

        buttons().defaults().size(200f, 50f);
        buttons().addButton("$text.cancel", this::hide);
        buttons().addButton("$text.editor.resize", () -> {
            cons.get(width, height);
            hide();
        });

    }
}

