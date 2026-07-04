package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.style.Drawable;
import arc.util.Align;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.game.EventType.ResizeEvent;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.net.Net;
import arc.Core;
import arc.Events;
import arc.scene.ui.Dialog;
import arc.scene.ui.ScrollPane;

import static io.anuke.mindustry.Vars.state;

public class FloatingDialog extends Dialog{
    private boolean wasPaused;
    protected boolean shouldPause;

    public FloatingDialog(String title){
        super(title);
        setFillParent(true);
        this.title.setAlignment(Align.center);
        titleTable.row();
        titleTable.image((Drawable)Core.atlas.getDrawable("white")).color(Palette.accent)
                .growX().height(3f).pad(4f);

        hidden(() -> {
            if(shouldPause && !state.is(State.menu)){
                if(!wasPaused || Net.active()){
                    state.set(State.playing);
                }
            }
        });

        shown(() -> {
            if(shouldPause && !state.is(State.menu)){
                wasPaused = state.is(State.paused);
                state.set(State.paused);
            }
        });

        boolean[] done = {false};

        shown(() -> Core.app.post(() ->
                forEach(child -> {
                    if(done[0]) return;

                    if(child instanceof ScrollPane){
                        Core.scene.setScrollFocus(child);
                        done[0] = true;
                    }
                })));
    }

    protected void onResize(Runnable run){
        Events.on(ResizeEvent.class, event -> {
            if(isShown()){
                run.run();
            }
        });
    }

    @Override
    public void addCloseButton(){
        buttons.button("$text.back", Core.atlas.getDrawable("icon-arrow-left"), 30f, this::hide).size(230f, 64f);

        keyDown(key -> {
            if(key == KeyCode.escape || key == KeyCode.back) {
                Core.app.post(this::hide);
            }
        });
    }
}

