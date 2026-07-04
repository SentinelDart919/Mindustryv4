package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.input.KeyCode;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.net.Net;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Table;
import arc.util.Strings;

import static io.anuke.mindustry.Vars.*;

public class PausedDialog extends FloatingDialog{
    private SaveDialog save = new SaveDialog();
    private LoadDialog load = new LoadDialog();
    private Table missionTable;

    public PausedDialog(){
        super("$text.menu");
        shouldPause = true;
        setup();

        shown(this::rebuild);

        keyDown(key -> {
            if(key == KeyCode.escape || key == KeyCode.back) {
                hide();
            }
        });
    }

    void rebuild(){
        missionTable.clear();
        missionTable.background((Drawable) null);
        if(world.getSector() != null){
            missionTable.background(Core.atlas.getDrawable("underline"));
            missionTable.add(Core.bundle.format("text.sector", world.getSector().x + ", " + world.getSector().y));
        }
    }

    void setup(){
        update(() -> {
            if(state.is(State.menu) && isShown()){
                hide();
            }
        });

        cont.table(t -> missionTable = t).colspan(mobile ? 3 : 2);
        cont.row();

        if(!mobile){
            float dw = 210f;
            cont.defaults().width(dw).height(50).pad(5f);

            cont.button("$text.back", this::hide).colspan(2).width(dw*2 + 20f);

            cont.row();
            cont.button("$text.unlocks", ui.unlocks::show);
            cont.button("$text.settings", ui.settings::show);

            cont.row();
            cont.button("$text.savegame", save::show).disabled(s -> world.getSector() != null);
            cont.button("$text.loadgame", load::show).disabled(b -> Net.active());

            cont.row();

            cont.button("$text.hostserver", ui.host::show).disabled(b -> Net.active()).colspan(2).width(dw*2 + 20f);

            cont.row();

            cont.button("$text.quit", () -> {
                ui.showConfirm("$text.confirm", "$text.quit.confirm", () -> {
                    if(Net.client()) netClient.disconnectQuietly();
                    runExitSave();
                    hide();
                });
            }).colspan(2).width(dw + 10f);

        }else{
            cont.defaults().size(120f).pad(5);
            float isize = 14f * 4;

            cont.buttonRow("$text.back", Core.atlas.getDrawable("icon-play-2"), () -> {
                hide();
            });
            cont.buttonRow("$text.settings", Core.atlas.getDrawable("icon-tools"), ui.settings::show);
            cont.buttonRow("$text.save", Core.atlas.getDrawable("icon-save"), save::show).disabled(b -> world.getSector() != null);

            cont.row();

            cont.buttonRow("$text.load", Core.atlas.getDrawable("icon-load"), load::show).disabled(b -> Net.active());
            cont.buttonRow("$text.hostserver.mobile", Core.atlas.getDrawable("icon-host"), ui.host::show).disabled(b -> Net.active());
            cont.buttonRow("$text.quit", Core.atlas.getDrawable("icon-quit"), () -> {
                ui.showConfirm("$text.confirm", "$text.quit.confirm", () -> {
                    if(Net.client()) netClient.disconnectQuietly();
                    runExitSave();
                    hide();
                });
            });
        }
    }

    public void runExitSave(){
        if(world.getSector() != null){
            world.sectors.refreshSectorPreview(world.getSector());
        }

        if(control.saves.getCurrent() == null ||
                !control.saves.getCurrent().isAutosave()){
            state.set(State.menu);
            return;
        }

        ui.loadLogic("$text.saveload", () -> {
            try{
                control.saves.getCurrent().save();
            }catch(Throwable e){
                e.printStackTrace();
                threads.runGraphics(() -> ui.showError("[accent]" + Core.bundle.get("text.savefail")));
            }
            state.set(State.menu);
        });
    }
}
