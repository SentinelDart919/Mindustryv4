package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import io.anuke.mindustry.net.Administration.PlayerInfo;

import static io.anuke.mindustry.Vars.*;

public class BansDialog extends FloatingDialog{

    public BansDialog(){
        super("$text.server.bans");

        addCloseButton();

        setup();

        shown(this::setup);
    }

    private void setup(){
        cont.clear();

        float w = 400f, h = 80f;

        Table table = new Table();

        ScrollPane pane = new ScrollPane(table);
        pane.setFadeScrollBars(false);

        if(netServer.admins.getBanned().size == 0){
            table.add("$text.server.bans.none");
        }

        for(PlayerInfo info : netServer.admins.getBanned()){
            Table res = new Table();
            res.margin(14f);

            res.labelWrap("IP: [LIGHT_GRAY]" + info.lastIP + "\n[]Name: [LIGHT_GRAY]" + info.lastName).width(w - h - 24f);
            res.add().growX();
            res.button((Drawable)Core.atlas.getDrawable("icon-cancel"), 14 * 3, () -> {
                ui.showConfirm("$text.confirm", "$text.confirmunban", () -> {
                    netServer.admins.unbanPlayerID(info.id);
                    setup();
                });
            }).size(h).pad(-14f);

            table.add(res).width(w).height(h);
            table.row();
        }

        cont.add(pane);
    }
}
