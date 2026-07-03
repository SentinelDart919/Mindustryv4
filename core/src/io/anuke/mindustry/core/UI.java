package io.anuke.mindustry.core;

import arc.Core;
import arc.input.KeyCode.Keys;
import arc.graphics.Color;
import arc.graphics.Colors;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.BitmapFont;
import arc.freetype.FreeTypeFontGenerator;
import arc.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import arc.math.Interp;
import arc.util.Align;

import static arc.Core.*;
import io.anuke.mindustry.editor.MapEditorDialog;
import io.anuke.mindustry.game.EventType.ResizeEvent;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.input.InputHandler;
import io.anuke.mindustry.ui.dialogs.*;
import arc.Core;
import io.anuke.mindustry.ui.fragments.*;
import arc.*;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.ApplicationListener;
import arc.scene.Group;
import arc.scene.Skin;
import arc.scene.actions.Actions;
import arc.graphics.g2d.Interpolation;
import arc.scene.ui.TooltipManager;
import arc.util.Timers;
import arc.scene.SceneModule;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextField;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.scene.ui.TooltipManager;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Scl;
import arc.util.Strings;
import arc.graphics.Gfx;

import static io.anuke.mindustry.Vars.*;
import static arc.scene.actions.Actions.*;

public class UI extends SceneModule{
    public Skin skin;
    private FreeTypeFontGenerator generator;

    public final MenuFragment menufrag = new MenuFragment();
    public final HudFragment hudfrag = new HudFragment();
    public final ChatFragment chatfrag = new ChatFragment();
    public final PlayerListFragment listfrag = new PlayerListFragment();
    public final BackgroundFragment backfrag = new BackgroundFragment();
    public final LoadingFragment loadfrag = new LoadingFragment();
    public final MapFragment mapfrag = new MapFragment();

    public AboutDialog about;
    public RestartDialog restart;
    public CustomGameDialog levels;
    public MapsDialog maps;
    public LoadDialog load;
    public DiscordDialog discord;
    public JoinDialog join;
    public HostDialog host;
    public PausedDialog paused;
    public SettingsMenuDialog settings;
    public ControlsDialog controls;
    public MapEditorDialog editor;
    public SchematicsDialog schematics;
    public LanguageDialog language;
    public BansDialog bans;
    public AdminsDialog admins;
    public TraceDialog traces;
    public ChangelogDialog changelog;
    public LocalPlayerDialog localplayers;
    public UnlocksDialog unlocks;
    public ContentInfoDialog content;
    public SectorsDialog sectors;
    public CampaignDialog campaigns;
    public MissionDialog missions;

    public UI(){
        Dialog.setShowAction(() -> sequence(
            alpha(0f),
            originCenter(),
            moveToAligned(Core.Gfx.getWidth() / 2f, Core.Gfx.getHeight() / 2f, Align.center),
            scaleTo(0.0f, 1f),
            parallel(
                scaleTo(1f, 1f, 0.1f, Interpolation.fade),
                fadeIn(0.1f, Interpolation.fade)
            )
        ));

        Dialog.setHideAction(() -> sequence(
            parallel(
                scaleTo(0.01f, 0.01f, 0.1f, Interpolation.fade),
                fadeOut(0.1f, Interpolation.fade)
            )
        ));

        TooltipManager.getInstance().animations = false;

        Core.settings.setErrorHandler(e -> Timers.run(1f, () -> showError("[crimson]Failed to access local storage.\nSettings will not be saved.")));

        Dialog.closePadR = -1;
        Dialog.closePadT = 5;

        Colors.put("accent", Palette.accent);
    }
    
    void generateFonts(){
        generator = new FreeTypeFontGenerator(Core.files.internal("fonts/pixel.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = (int)(14*2 * Math.max(Scl.scl(1f), 0.5f));
        param.shadowColor = Color.darkGray;
        param.shadowOffsetY = 2;
        param.incremental = true;

        skin.add("default-font", generator.generateFont(param));
        skin.add("default-font-chat", generator.generateFont(param));
        skin.getFont("default-font").getData().markupEnabled = true;
        skin.getFont("default-font").setOwnsTexture(false);
    }

    @Override
    protected void loadSkin(){
        skin = new Skin(Core.atlas);
        generateFonts();
        skin.load(Core.files.internal("ui/uiskin.json"));

        for(BitmapFont font : skin.getAll(BitmapFont.class).values()){
            font.setUseIntegerPositions(true);
            //font.getData().setScale(Vars.fontScale);
        }
    }

    @Override
    public void update(){
        if(disableUI) return;

        if(Graphics.drawing()) Gfx.end();

        act();

        Gfx.begin();

        for(int i = 0; i < players.length; i++){
            InputHandler input = control.input(i);

            if(input.isCursorVisible()){
                Draw.color();

                float scl = Scl.scl(3f);

                Draw.rect("controller-cursor", input.getMouseX(), Core.Gfx.getHeight() - input.getMouseY(), 16 * scl, 16 * scl);
            }
        }

        Gfx.end();
        Draw.color();
    }

    @Override
    public void init(){
        editor = new MapEditorDialog();
        controls = new ControlsDialog();
        restart = new RestartDialog();
        join = new JoinDialog();
        discord = new DiscordDialog();
        load = new LoadDialog();
        levels = new CustomGameDialog();
        language = new LanguageDialog();
        unlocks = new UnlocksDialog();
        settings = new SettingsMenuDialog();
        schematics = new SchematicsDialog();
        host = new HostDialog();
        paused = new PausedDialog();
        changelog = new ChangelogDialog();
        about = new AboutDialog();
        bans = new BansDialog();
        admins = new AdminsDialog();
        traces = new TraceDialog();
        maps = new MapsDialog();
        localplayers = new LocalPlayerDialog();
        content = new ContentInfoDialog();
        sectors = new SectorsDialog();
        campaigns = new CampaignDialog();
        missions = new MissionDialog();

        Group group = Core.scene.getRoot();

        backfrag.build(group);
        control.input(0).getFrag().build(Core.scene.getRoot());
        hudfrag.build(group);
        menufrag.build(group);
        chatfrag.container().build(group);
        listfrag.build(group);
        loadfrag.build(group);
        mapfrag.build(group);
    }

    @Override
    public void resize(int width, int height){
        super.resize(width, height);

        Events.fire(new ResizeEvent());
    }

    @Override
    public void dispose(){
        super.dispose();
        generator.dispose();
    }

    public void loadGraphics(Runnable call){
        loadGraphics("$text.loading", call);
    }

    public void loadGraphics(String text, Runnable call){
        loadfrag.show(text);
        Timers.runTask(7f, () -> {
            call.run();
            loadfrag.hide();
        });
    }

    public void loadLogic(Runnable call){
        loadLogic("$text.loading", call);
    }

    public void loadLogic(String text, Runnable call){
        loadfrag.show(text);
        Timers.runTask(7f, () ->
            threads.run(() -> {
                call.run();
                threads.runGraphics(loadfrag::hide);
            }));
    }

    public boolean hasDialog(){
        return Core.scene.hasDialog();
    }

    public void showTextInput(String title, String text, String def, TextFieldFilter filter, Cons<String> confirmed){
        new Dialog(title, "dialog"){{
            content().margin(30).add(text).padRight(6f);
            TextField field = content().addField(def, t -> {
            }).size(170f, 50f).get();
            field.setTextFieldFilter((f, c) -> field.getText().length() < 12 && filter.acceptChar(f, c));
            Platform.instance.addDialog(field);
            buttons().defaults().size(120, 54).pad(4);
            buttons().addButton("$text.ok", () -> {
                confirmed.get(field.getText());
                hide();
            }).disabled(b -> field.getText().isEmpty());
            buttons().addButton("$text.cancel", this::hide);
        }}.show();
    }

    public void showTextInput(String title, String text, String def, Cons<String> confirmed){
        showTextInput(title, text, def, (field, c) -> true, confirmed);
    }

    public void showInfoFade(String info){
        Table table = new Table();
        table.setFillParent(true);
        table.actions(Actions.fadeOut(7f, Interpolation.fade), Actions.removeActor());
        table.top().add(info).padTop(8);
        Core.scene.add(table);
    }

    public void showInfo(String info){
        new Dialog("", "dialog"){{
            getCell(content()).growX();
            content().margin(15).add(info).width(400f).wrap().get().setAlignment(Align.center, Align.center);
            buttons().addButton("$text.ok", this::hide).size(90, 50).pad(4);
        }}.show();
    }

    public void showInfo(String info, Runnable clicked){
        new Dialog("", "dialog"){{
            getCell(content()).growX();
            content().margin(15).add(info).width(400f).wrap().get().setAlignment(Align.center, Align.center);
            buttons().addButton("$text.ok", () -> {
                clicked.run();
                hide();
            }).size(90, 50).pad(4);
        }}.show();
    }

    public void showError(String text){
        new Dialog("$text.error.title", "dialog"){{
            content().margin(15).add(text).width(400f).wrap().get().setAlignment(Align.center, Align.center);
            buttons().addButton("$text.ok", this::hide).size(90, 50).pad(4);
        }}.show();
    }

    public void showText(String title, String text){
        new Dialog(title, "dialog"){{
            content().margin(15).add(text).width(400f).wrap().get().setAlignment(Align.center, Align.center);
            buttons().addButton("$text.ok", this::hide).size(90, 50).pad(4);
        }}.show();
    }

    public void showConfirm(String title, String text, Runnable confirmed){
        FloatingDialog dialog = new FloatingDialog(title);
        dialog.content().add(text).width(400f).wrap().pad(4f).get().setAlignment(Align.center, Align.center);
        dialog.buttons().defaults().size(200f, 54f).pad(2f);
        dialog.setFillParent(false);
        dialog.buttons().addButton("$text.cancel", dialog::hide);
        dialog.buttons().addButton("$text.ok", () -> {
            dialog.hide();
            confirmed.run();
        });
        dialog.keyDown(Keys.ESCAPE, dialog::hide);
        dialog.keyDown(Keys.BACK, dialog::hide);
        dialog.show();
    }

    public String formatAmount(int number){
        if(number >= 1000000){
            return Strings.toFixed(number / 1000000f, 1) + "[gray]mil[]";
        }else if(number >= 10000){
            return number / 1000 + "[gray]k[]";
        }else if(number >= 1000){
            return Strings.toFixed(number / 1000f, 1) + "[gray]k[]";
        }else{
            return number + "";
        }
    }
}



