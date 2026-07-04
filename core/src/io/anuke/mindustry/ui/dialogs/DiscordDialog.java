package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.graphics.Color;
import arc.scene.style.Drawable;
import io.anuke.mindustry.graphics.Palette;
import arc.scene.ui.Dialog;

import static io.anuke.mindustry.Vars.discordURL;
import static io.anuke.mindustry.Vars.ui;

public class DiscordDialog extends Dialog{

    public DiscordDialog(){
        super("");

        float h = 70f;

        cont.margin(12f);

        Color color = Color.valueOf("7289da");

        cont.table(t -> {
            t.background(Core.atlas.getDrawable("button")).margin(0);

            t.table(img -> {
                img.image((Drawable)Core.atlas.getDrawable("white")).height(h - 5).width(40f).color(color);
                img.row();
                img.image((Drawable)Core.atlas.getDrawable("white")).height(5).width(40f).color(color.cpy().mul(0.8f, 0.8f, 0.8f, 1f));
            }).expandY();

            t.table(i -> {
                i.background(Core.atlas.getDrawable("button"));
                i.image((Drawable)Core.atlas.getDrawable("icon-discord")).size(14 * 3);
            }).size(h).left();

            t.add("$text.discord").color(Palette.accent).growX().padLeft(10f);
        }).size(470f, h).pad(10f);

        buttons.defaults().size(170f, 50);

        buttons.button("$text.back", this::hide);
        buttons.button("$text.copylink", () -> {
            Core.app.setClipboardText(discordURL);
        });
        buttons.button("$text.openlink", () -> {
            if(!Core.app.openURI(discordURL)){
                ui.showError("$text.linkfail");
                Core.app.setClipboardText(discordURL);
            }
        });
    }
}
