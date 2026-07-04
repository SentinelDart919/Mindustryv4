package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.graphics.Color;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.net.Net;
import arc.util.Time;
import arc.util.Strings;
import arc.scene.ui.ImageButton;


import java.io.IOException;

import static io.anuke.mindustry.Vars.players;
import static io.anuke.mindustry.Vars.ui;

public class HostDialog extends FloatingDialog{
    float w = 300;

    public HostDialog(){
        super("$text.hostserver");

        Player player = players[0];

        addCloseButton();

        cont.table(t -> {
            t.add("$text.name").padRight(10);
            t.addField(Core.settings.getString("name"), text -> {
                player.name = text;
                Core.settings.put("name", text);
                Core.settings.save();
                ui.listfrag.rebuild();
            }).grow().pad(8).get().setMaxLength(40);

            ImageButton button = t.button(Core.atlas.getDrawable("white"), 40, () -> {
                new ColorPickDialog().show(color -> {
                    player.color.set(color);
                    Core.settings.putInt("color-0", Color.rgba8888(color));
                    Core.settings.save();
                });
            }).size(54f).get();
            button.update(() -> button.getStyle().imageUpColor = player.color);
        }).width(w).height(70f).pad(4).colspan(3);

        cont.row();

        cont.add().width(65f);

        cont.button("$text.host", () -> {
            if(Core.settings.getString("name").trim().isEmpty()){
                ui.showInfo("$text.noname");
                return;
            }

            ui.loadfrag.show("$text.hosting");
            Time.runTask(5f, () -> {
                try{
                    Net.host(Vars.port);
                    player.isAdmin = true;
                }catch(IOException e){
                    ui.showError(Core.bundle.format("text.server.error", Strings.parseException(e, false)));
                }
                ui.loadfrag.hide();
                hide();
            });
        }).width(w).height(70f);

        cont.button("?", () -> ui.showInfo("$text.host.info")).size(65f, 70f).padLeft(6f);
    }
}
