package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.maps.Sector;
import arc.util.Strings;

import static io.anuke.mindustry.Vars.*;

public class RestartDialog extends FloatingDialog{
    private Team winner;

    public RestartDialog(){
        super("$text.gameover");
        setFillParent(false);
        shown(this::rebuild);
    }

    public void show(Team winner){
        this.winner = winner;
        show();
    }

    void rebuild(){
        buttons.clear();
        cont.clear();

        buttons.margin(10);

        if(state.mode.isPvp){
            cont.add(Core.bundle.format("text.gameover.pvp",winner.localized())).pad(6);
            buttons.button("$text.menu", () -> {
                hide();
                state.set(State.menu);
                logic.reset();
            }).size(130f, 60f);
        }else if(world.getSector() == null){
            if(control.isHighScore()){
                cont.add("$text.highscore").pad(6);
                cont.row();
            }
            cont.add(Core.bundle.format("text.wave.lasted", state.wave)).pad(12);

            buttons.button("$text.menu", () -> {
                hide();
                state.set(State.menu);
                logic.reset();
            }).size(130f, 60f);
        }else{
            cont.add("$text.sector.gameover");
            buttons.button("$text.menu", () -> {
                if(world.getSector() != null){
                    world.sectors.abandonSector(world.getSector(), true);
                }
                hide();
                state.set(State.menu);
                logic.reset();
            }).size(130f, 60f);

            buttons.button("$text.sector.retry", () -> {
                Sector sector = world.getSector();
                ui.loadLogic(() -> world.sectors.playSector(sector));
                hide();
            }).size(130f, 60f);
        }
    }
}
