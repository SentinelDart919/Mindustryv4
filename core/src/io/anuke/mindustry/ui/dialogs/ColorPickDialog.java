package io.anuke.mindustry.ui.dialogs;

import arc.input.KeyCode;
import arc.graphics.Color;
import arc.func.Cons;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.playerColors;
import static io.anuke.mindustry.Vars.players;

public class ColorPickDialog extends Dialog{
    private Cons<Color> cons;

    public ColorPickDialog(){
        super("", "dialog");
        build();
    }

    private void build(){
        Table table = new Table();
        content().add(table);

        for(int i = 0; i < playerColors.length; i++){
            Color color = playerColors[i];

            ImageButton button = table.addImageButton("white", "clear-toggle", 34, () -> {
                cons.get(color);
                hide();
            }).size(48).get();
            button.setChecked(players[0].color.equals(color));
            button.getStyle().imageUpColor = color;

            if(i % 4 == 3){
                table.row();
            }
        }

        keyDown(key -> {
            if(key == KeyCode.escape || key == KeyCode.back)
                hide();
        });

    }

    public void show(Cons<Color> cons){
        this.cons = cons;
        show();
    }
}
