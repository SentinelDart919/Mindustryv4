package io.anuke.mindustry.ui.dialogs;

import arc.Core;
import arc.input.KeyCode;
import arc.files.Fi;
import arc.util.Align;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.ai.MassAI;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.net.Net;
import arc.func.Cons;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Image;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.SettingsDialog.SettingsTable;
import arc.scene.ui.SettingsDialog.SettingsTable.Setting;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.math.Mathf;

import java.util.HashMap;
import java.util.Map;

import static io.anuke.mindustry.Vars.*;

public class SettingsMenuDialog extends FloatingDialog{
    public SettingsTable graphics;
    public SettingsTable game;
    public SettingsTable sound;

    private Table prefs;
    private Table menu;
    private boolean wasPaused;

    public SettingsMenuDialog(){
        super("$text.settings.title");

        hidden(() -> {
            if(!state.is(State.menu)){
                if(!wasPaused || Net.active())
                    state.set(State.playing);
            }
        });

        shown(() -> {
            if(!state.is(State.menu)){
                wasPaused = state.is(State.paused);
                state.set(State.paused);
            }
        });

        setFillParent(true);
        title.setAlignment(Align.center);
        titleTable.row();
        titleTable.add(new Image((Drawable)Core.atlas.getDrawable("white")))
                .growX().height(3f).pad(4f).get().setColor(Palette.accent);

        cont.clearChildren();
        cont.remove();
        buttons.remove();

        menu = new Table();

        Cons<SettingsTable> s = table -> {
            table.row();
            table.addImageTextButton("$text.back", "icon-arrow-left", 10 * 3, this::back).size(240f, 60f).colspan(2).padTop(15f);
        };

        game = new SettingsTable(s);
        graphics = new SettingsTable(s);
        sound = new SettingsTable(s);

        prefs = new Table();
        prefs.top();
        prefs.margin(14f);

        menu.defaults().size(300f, 60f).pad(3f);
        menu.button("$text.settings.game", () -> visible(0));
        menu.row();
        menu.button("$text.settings.graphics", () -> visible(1));
        menu.row();
        menu.button("$text.settings.sound", () -> visible(2));
        if(!Vars.mobile){
            menu.row();
            menu.button("$text.settings.controls", ui.controls::show);
        }
        menu.row();
        menu.button("$text.settings.language", ui.language::show);

        prefs.clearChildren();
        prefs.add(menu);

        ScrollPane pane = new ScrollPane(prefs);
        pane.addCaptureListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                Element actor = pane.hit(x, y, true);
                if(actor instanceof Slider){
                    pane.setFlickScroll(false);
                    return true;
                }

                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                pane.setFlickScroll(true);
                super.touchUp(event, x, y, pointer, button);
            }
        });
        pane.setFadeScrollBars(false);

        row();
        add(pane).grow().top();
        row();
        add(buttons).fillX();

        hidden(this::back);

        addSettings();
    }

    void addSettings(){
        sound.volumePrefs();
        sound.finish();

        game.screenshakePref();
        game.checkPref("effects", true);
        if(mobile){
            game.checkPref("autotarget", true);
        }
        game.sliderPref("saveinterval", 120, 10, 5 * 120, i -> Core.bundle.format("setting.seconds", i));
        game.checkPref("planet3d", true);
        game.checkPref("massai-debug", false, MassAI::setDebug);

        if(!mobile){
            game.checkPref("crashreport", true);
        }

        game.pref(new Setting(){
            @Override
            public void add(SettingsTable table){
                table.addButton("$text.settings.cleardata", () -> {
                    FloatingDialog dialog = new FloatingDialog("$text.settings.cleardata");
                    dialog.setFillParent(false);
                    dialog.cont.defaults().size(230f, 60f).pad(3);
                    dialog.addCloseButton();
                    dialog.cont.button("$text.settings.clearsectors", () -> {
                        ui.showConfirm("$text.confirm", "$text.settings.clear.confirm", () -> {
                            world.sectors.clear();
                            dialog.hide();
                        });
                    });
                    dialog.cont.row();
                    dialog.cont.button("$text.settings.clearunlocks", () -> {
                        ui.showConfirm("$text.confirm", "$text.settings.clear.confirm", () -> {
                            control.unlocks.reset();
                            dialog.hide();
                        });
                    });
                    dialog.cont.row();
                    dialog.cont.button("$text.settings.clearall", () -> {
                        ui.showConfirm("$text.confirm", "$text.settings.clearall.confirm", () -> {
                            Map<String, Object> map = new HashMap<>();
                            for(String value : Core.settings.keys()){
                                if(value.contains("usid") || value.contains("uuid")){
                                    map.put(value, Core.settings.getString(value));
                                }
                            }
                            Core.settings.clear();

                            for(Map.Entry<String, Object> entry : map.entrySet()){
                                Core.settings.put(entry.getKey(), entry.getValue());
                            }
                            Core.settings.manualSave();

                            for(Fi file : dataDirectory.list()){
                                file.deleteDirectory();
                            }

                            Core.app.exit();
                        });
                    });
                    dialog.cont.row();
                    dialog.show();
                }).size(220f, 60f).pad(6).left();
                table.add();
                table.row();
            }
        });
        game.finish();

        graphics.sliderPref("fpscap", 125, 5, 125, 5, s -> (s > 120 ? Core.bundle.get("setting.fpscap.none") : Core.bundle.format("setting.fpscap.text", s)));

        if(!mobile){
            graphics.checkPref("vsync", true, b -> Core.graphics.setVSync(b));
            graphics.checkPref("fullscreen", false, b -> {
                if(b){
                    Core.graphics.setFullscreen(true);
                }else{
                    Core.graphics.setWindowSize(600, 480);
                }
            });

            Core.graphics.setVSync(Core.settings.getBool("vsync"));
            if(Core.settings.getBool("fullscreen")){
                Core.graphics.setFullscreen(true);
            }
        }

        graphics.checkPref("fps", false);
        graphics.checkPref("indicators", true);
        graphics.checkPref("lasers", true);
        graphics.checkPref("minimap", !mobile);
        graphics.finish();
    }

    private void back(){
        prefs.clearChildren();
        prefs.add(menu);
    }

    private void visible(int index){
        prefs.clearChildren();
        Table table = Mathf.select(index, game, graphics, sound);
        prefs.add(table);
    }

    @Override
    public void addCloseButton(){
        buttons.button("$text.menu", Core.atlas.getDrawable("icon-arrow-left"), 30f, this::hide).size(230f, 64f);

        keyDown(key -> {
            if(key == KeyCode.escape || key == KeyCode.back)
                hide();
        });
    }
}
