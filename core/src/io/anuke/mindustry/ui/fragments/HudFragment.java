package io.anuke.mindustry.ui.fragments;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.util.Align;
import arc.util.Bundles;
import arc.struct.Seq;
import arc.util.Scaling;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.game.EventType.StateChangeEvent;
import io.anuke.mindustry.game.GameMode;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.maps.missions.WaveExtraMission;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.net.Packets.AdminAction;
import io.anuke.mindustry.entities.units.UnitOrderType;
import io.anuke.mindustry.input.DesktopInput;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.ui.IntFormat;
import io.anuke.mindustry.ui.Minimap;
import io.anuke.mindustry.ui.dialogs.FloatingDialog;
import arc.*;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Scl;
import arc.graphics.Hue;
import arc.math.Mathf;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.Button.ButtonStyle;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.ScrollPane.ScrollPaneStyle;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Collapser;
import arc.util.Inputs;
import arc.util.Time;
import arc.util.Timers;
import arc.util.pooling.Pools;

import static io.anuke.mindustry.Vars.*;

public class HudFragment extends Fragment{
    public final PlacementFragment blockfrag = new PlacementFragment();

    private ImageButton menu, flip;
    private Stack wavetable;
    private Table infolabel;
    private Table lastUnlockTable;
    private Table lastUnlockLayout;
    private boolean shown = true;
    private float dsize = 58;
    private float isize = 40;

    private float coreAttackTime;
    private float lastCoreHP;
    private float coreAttackOpacity = 0f;

    private float biomassAlertTime;
    private float biomassAlertOpacity = 0f;
    private String biomassAlertText = "";

    public void build(Group parent){

        //menu at top left
        parent.fill(cont -> {

            cont.top().left().visible(() -> !state.is(State.menu));

            if(mobile){
                cont.table(select -> {
                    select.left();
                    select.defaults().size(dsize).left();

                    menu = (ImageButton)select.button(Core.scene.getSkin().getDrawable("icon-menu"), Core.scene.getSkin().get("clear", ImageButtonStyle.class), isize, ui.paused::show).get();
                    flip = (ImageButton)select.button(Core.scene.getSkin().getDrawable("icon-arrow-up"), Core.scene.getSkin().get("clear", ImageButtonStyle.class), isize, this::toggleMenus).get();

                    select.button(Core.scene.getSkin().getDrawable("icon-pause"), Core.scene.getSkin().get("clear", ImageButtonStyle.class), isize, () -> {
                        if(Net.active()){
                            ui.listfrag.toggle();
                        }else{
                            state.set(state.is(State.paused) ? State.playing : State.paused);
                        }
                    }).update(i -> {
                        ImageButtonStyle style = ((ImageButton)i).getStyle();
                        if(Net.active()){
                            style.imageUp = Core.scene.getSkin().getDrawable("icon-players");
                        }else{
                            i.setDisabled(Net.active());
                            style.imageUp = Core.scene.getSkin().getDrawable(state.is(State.paused) ? "icon-play" : "icon-pause");
                        }
                    }).get();

                    select.button(Core.scene.getSkin().getDrawable("icon-settings"), Core.scene.getSkin().get("clear", ImageButtonStyle.class), isize, () -> {
                        if(Net.active() && mobile){
                            if(ui.chatfrag.chatOpen()){
                                ui.chatfrag.hide();
                            }else{
                                ui.chatfrag.toggle();
                            }
                        }else{
                            ui.unlocks.show();
                        }
                    }).update(i -> {
                        ImageButtonStyle style = ((ImageButton)i).getStyle();
                        if(Net.active() && mobile){
                            style.imageUp = Core.scene.getSkin().getDrawable("icon-chat");
                        }else{
                            style.imageUp = Core.scene.getSkin().getDrawable("icon-unlocks");
                        }
                    }).get();

                    select.button(Core.scene.getSkin().getDrawable("icon-copy"), Core.scene.getSkin().get("clear", ImageButtonStyle.class), isize, ui.schematics::show).get();

                    select.image("blank").color(Palette.accent).width(6f).fillY();
                });

                cont.row();
                cont.image("blank").height(6f).color(Palette.accent).fillX();
                cont.row();
            }

            cont.update(() -> {
                if(Inputs.keyTap("toggle_menus") && !ui.chatfrag.chatOpen()){
                    toggleMenus();
                }
            });

            Stack stack = new Stack();
            TextButton waves = new TextButton("", Core.scene.getSkin().get("wave", TextButtonStyle.class));
            Table btable = new Table().margin(0);

            stack.add(waves);
            stack.add(btable);

            wavetable = stack;

            addWaveTable(waves);
            addPlayButton(btable);
            cont.add(stack).width(dsize * 4 + 6f);

            cont.row();

            //fps display
            infolabel = cont.table(t -> {
                IntFormat fps = new IntFormat("text.fps");
                IntFormat tps = new IntFormat("text.tps");
                IntFormat ping = new IntFormat("text.ping");
                t.label(() -> fps.get(Core.graphics.getFramesPerSecond())).padRight(10);
                t.row();
                if(netServer.isWaitingForPlayers()){
                    t.label(() -> ping.get(Net.getPing())).visible(Net::client).colspan(2);
                }
            }).size(-1).visible(() -> Core.settings.getBool("fps")).update(t -> t.setTranslation(0, (!waves.isVisible() ? wavetable.getHeight() : Math.min(wavetable.getTranslation().y, wavetable.getHeight())) )).get();

            //make wave box appear below rest of menu
            if(mobile){
                cont.swapActor(wavetable, menu.getParent());
            }
        });

        //minimap
        parent.fill(t -> t.top().right().add(new Minimap())
            .visible(() -> !state.is(State.menu) && Core.settings.getBool("minimap") && !ui.mapfrag.isOpen()));

        //paused table
        parent.fill(t -> {
            t.top().visible(() -> state.is(State.paused) && !Net.active());
            t.table(Core.scene.getStyle(ButtonStyle.class).up, top -> top.add("$text.paused").pad(6f));
        });

        parent.fill(t -> {
            t.visible(() -> netServer.isWaitingForPlayers() && !state.is(State.menu));
            t.table(Core.scene.getStyle(ButtonStyle.class).up, c -> c.add("$text.waiting.players"));
        });

        //'core is under attack' table
        parent.fill(t -> {
            float notifDuration = 240f;

            Events.on(StateChangeEvent.class, event -> {
                if(event.to == State.menu || event.from == State.menu){
                    coreAttackTime = 0f;
                    lastCoreHP = Float.NaN;
                }
            });

            t.top().visible(() -> {
                if(state.is(State.menu) || state.teams.get(players[0].getTeam()).cores.size == 0 ||
                state.teams.get(players[0].getTeam()).cores.first().entity == null){
                    coreAttackTime = 0f;
                    return false;
                }

                float curr = state.teams.get(players[0].getTeam()).cores.first().entity.health;
                if(!Float.isNaN(lastCoreHP) && curr < lastCoreHP){
                    coreAttackTime = notifDuration;
                }
                lastCoreHP = curr;

                t.getColor().a = coreAttackOpacity;
                if(coreAttackTime > 0){
                    coreAttackOpacity = Mathf.lerpDelta(coreAttackOpacity, 1f, 0.1f);
                }else{
                    coreAttackOpacity = Mathf.lerpDelta(coreAttackOpacity, 0f, 0.1f);
                }

                coreAttackTime -= Timers.delta();

                return coreAttackOpacity > 0;
            });
            t.table(Core.scene.getStyle(ButtonStyle.class).up, top -> top.add("$text.coreattack").pad(2)
            .update(label -> label.setColor(Hue.mix(Color.orange, Color.scarlet, Mathf.absin(Timers.time(), 2f, 1f)))));
        });

        //'biomass alert' table
        parent.fill(t -> {
            t.top().visible(() -> {
                if(state.is(State.menu)){
                    biomassAlertTime = 0f;
                    return false;
                }

                t.getColor().a = biomassAlertOpacity;
                if(biomassAlertTime > 0){
                    biomassAlertOpacity = Mathf.lerpDelta(biomassAlertOpacity, 1f, 0.1f);
                }else{
                    biomassAlertOpacity = Mathf.lerpDelta(biomassAlertOpacity, 0f, 0.1f);
                }

                biomassAlertTime -= Timers.delta();

                return biomassAlertOpacity > 0;
            });
            t.table(Core.scene.getStyle(ButtonStyle.class).up, top -> top.add("").pad(2)
                    .update(label -> {
                        ((Label)label).setText(biomassAlertText);
                    label.setColor(Hue.mix(Color.scarlet, Color.purple, Mathf.absin(Timers.time(), 2f, 1f)));
                    }));
        });

        //'saving' indicator
        parent.fill(t -> {
            t.bottom().visible(() -> !state.is(State.menu) && control.saves.isSaving());
            t.add("$text.saveload");
        });

        //desktop RTS command panel
        parent.fill(t -> {
            t.bottom().left().visible(() ->
                !mobile &&
                !state.is(State.menu) &&
                control.input(0) instanceof DesktopInput &&
                ((DesktopInput)control.input(0)).isUnitCommandMode()
            );

            t.table(Core.scene.getStyle(ButtonStyle.class).up, pane -> {
                pane.left().margin(6f);
                pane.label(() -> {
                    DesktopInput input = (DesktopInput)control.input(0);
                    UnitOrderType type = input.getActiveOrderType();
                    return "Units: " + (type == UnitOrderType.attackMove ? "Attack-Move" : type == UnitOrderType.move ? "Move" : "Clear");
                }).padRight(8f);

                pane.button("Move", Core.scene.getSkin().get("clear-partial", TextButtonStyle.class), () -> ((DesktopInput)control.input(0)).setActiveOrderType(UnitOrderType.move))
                    .size(78f, 42f)
                    .update(b -> b.setChecked(((DesktopInput)control.input(0)).getActiveOrderType() == UnitOrderType.move));

                pane.button("Attack", Core.scene.getSkin().get("clear-partial", TextButtonStyle.class), () -> ((DesktopInput)control.input(0)).setActiveOrderType(UnitOrderType.attackMove))
                    .size(78f, 42f)
                    .padLeft(4f)
                    .update(b -> b.setChecked(((DesktopInput)control.input(0)).getActiveOrderType() == UnitOrderType.attackMove));

                pane.button("Clear", Core.scene.getSkin().get("clear-partial", TextButtonStyle.class), () -> ((DesktopInput)control.input(0)).clearUnitSelection())
                    .size(78f, 42f)
                    .padLeft(4f);

                pane.button("Normal", Core.scene.getSkin().get("clear-partial", TextButtonStyle.class), () -> ((DesktopInput)control.input(0)).setSelectedDronesFollow(false))
                    .size(84f, 42f)
                    .padLeft(8f)
                    .visible(() -> ((DesktopInput)control.input(0)).hasSelectedDrones())
                    .update(b -> b.setChecked(!((DesktopInput)control.input(0)).selectedDronesFollowing()));

                pane.button("Follow", Core.scene.getSkin().get("clear-partial", TextButtonStyle.class), () -> ((DesktopInput)control.input(0)).setSelectedDronesFollow(true))
                    .size(84f, 42f)
                    .padLeft(4f)
                    .visible(() -> ((DesktopInput)control.input(0)).hasSelectedDrones())
                    .update(b -> b.setChecked(((DesktopInput)control.input(0)).selectedDronesFollowing()));
            }).margin(8f);
        });

        blockfrag.build(Core.scene.getRoot());
    }

    public void showBiomassAlert(String text){
        this.biomassAlertText = text;
        this.biomassAlertTime = 600f;
    }

    public void showToast(String text){
        Table table = new Table();
        table.update(() -> {
            if(state.is(State.menu)){
                table.remove();
            }
        });
        table.margin(12);
        table.image("icon-check").size(16*2).pad(3);
        table.add(text).wrap().width(280f).get().setAlignment(Align.center, Align.center);
        table.pack();

        //create container table which will align and move
        Table container = new Table();
        Core.scene.add(container);
        container.top().add(table);
        container.setTranslation(0, table.getPrefHeight());
        container.actions(Actions.translateBy(0, -table.getPrefHeight(), 1f, Interp.fade), Actions.delay(4f),
        //nesting actions() calls is necessary so the right prefHeight() is used
        Actions.run(() -> container.actions(Actions.translateBy(0, table.getPrefHeight(), 1f, Interp.fade), Actions.removeActor())));
    }

    /**Show unlock notification for a new recipe.*/
    public void showUnlock(Recipe recipe){

        //if there's currently no unlock notification...
        if(lastUnlockTable == null){
            Table table = new Table();
            table.update(() -> {
                if(state.is(State.menu)){
                    table.remove();
                    lastUnlockLayout = null;
                    lastUnlockTable = null;
                }
            });
            table.margin(12);

            Table in = new Table();

            //create texture stack for displaying
            Stack stack = new Stack();
            for(TextureRegion region : recipe.result.getCompactIcon()){
                Image image = new Image(region);
                image.setScaling(Scaling.fit);
                stack.add(image);
            }

            in.add(stack).size(48f).pad(2);

            //add to table
            table.add(in).padRight(8);
            table.add("$text.unlocked");
            table.pack();

            //create container table which will align and move
            Table container = Core.scene.table();
            container.top().add(table);
            container.setTranslation(0, table.getPrefHeight());
            container.actions(Actions.translateBy(0, -table.getPrefHeight(), 1f, Interp.fade), Actions.delay(4f),
                    //nesting actions() calls is necessary so the right prefHeight() is used
                    Actions.run(() -> container.actions(Actions.translateBy(0, table.getPrefHeight(), 1f, Interp.fade), Actions.run(() -> {
                        lastUnlockTable = null;
                        lastUnlockLayout = null;
                    }), Actions.removeActor())));

            lastUnlockTable = container;
            lastUnlockLayout = in;
        }else{
            //max column size
            int col = 3;
            //max amount of elements minus extra 'plus'
            int cap = col * col - 1;

            //get old elements
            Seq<Element> elements = new Seq<>(lastUnlockLayout.getChildren());
            int esize = elements.size;

            //...if it's already reached the cap, ignore everything
            if(esize > cap) return;

            //get size of each element
            float size = 48f / Math.min(elements.size + 1, col);

            //correct plurals if needed
            if(esize == 1){
                ((Label) lastUnlockLayout.getParent().find(e -> e instanceof Label)).setText("$text.unlocked.plural");
            }

            lastUnlockLayout.clearChildren();
            lastUnlockLayout.defaults().size(size).pad(2);

            for(int i = 0; i < esize && i <= cap; i++){
                lastUnlockLayout.add(elements.get(i));

                if(i % col == col - 1){
                    lastUnlockLayout.row();
                }
            }

            //if there's space, add it
            if(esize < cap){

                Stack stack = new Stack();
                for(TextureRegion region : recipe.result.getCompactIcon()){
                    Image image = new Image(region);
                    image.setScaling(Scaling.fit);
                    stack.add(image);
                }

                lastUnlockLayout.add(stack);
            }else{ //else, add a specific icon to denote no more space
                lastUnlockLayout.image("icon-add");
            }

            lastUnlockLayout.pack();
        }
    }

    public void showTextDialog(String str){
        new FloatingDialog("$text.mission.info"){{
            shouldPause = true;
            setFillParent(false);
            getCell(content()).growX();
            content().margin(15).add(str).width(400f).wrap().get().setAlignment(Align.left, Align.left);
            buttons().addButton("$text.continue", this::hide).size(140, 60).pad(4);
        }}.show();
    }

    private void toggleMenus(){
        wavetable.clearActions();
        infolabel.clearActions();

        float dur = 0.3f;
        Interp in = Interp.pow3Out;

        if(flip != null){
            flip.getStyle().imageUp = Core.scene.getSkin().getDrawable(shown ? "icon-arrow-down" : "icon-arrow-up");
        }

        if(shown){
            shown = false;
            blockfrag.toggle(dur, in);
            wavetable.actions(Actions.translateBy(0, (wavetable.getHeight() + Scl.scl(dsize) + Scl.scl(6)) - wavetable.getTranslation().y, dur, in));
            infolabel.actions(Actions.translateBy(0, (wavetable.getHeight()) - wavetable.getTranslation().y, dur, in));
        }else{
            shown = true;
            blockfrag.toggle(dur, in);
            wavetable.actions(Actions.translateBy(0, -wavetable.getTranslation().y, dur, in));
            infolabel.actions(Actions.translateBy(0, -infolabel.getTranslation().y, dur, in));
        }
    }

    private void addWaveTable(TextButton table){

        IntFormat wavef = new IntFormat("text.wave");
        IntFormat enemyf = new IntFormat("text.wave.enemy");
        IntFormat enemiesf = new IntFormat("text.wave.enemies");

        table.clearChildren();
        table.touchable = Touchable.enabled;

        table.labelWrap(() ->
            world.getSector() == null ?
                ((state.enemies() > 0 && state.mode.disableWaveTimer ?
                wavef.get(state.wave) + "\n" + (state.enemies() == 1 ?
                    enemyf.get(state.enemies()) :
                    enemiesf.get(state.enemies())) :
                wavef.get(state.wave) + "\n" +
                    (!state.mode.disableWaveTimer || state.mode == GameMode.SiegeMode ?
                    Bundles.format("text.wave.waiting", (int)(state.wavetime/60)) :
                    Bundles.get("text.waiting"))) +
                (state.mode == GameMode.SiegeMode ?
                    "\n" + Bundles.format("text.mission.enemyfunds", WaveExtraMission.displayedFunds) +
                    "\n" + Bundles.format("text.mission.enemyfunds.next", WaveExtraMission.nextWaveFundsGain(state.wave, state.difficulty)) : "")) :
            Bundles.format("text.mission.display", world.getSector().currentMission().displayString())).growX().pad(8f);

        table.clicked(() -> {
            if(world.getSector() != null && world.getSector().currentMission().hasMessage()){
                world.getSector().currentMission().showMessage();
            }
        });

        table.setDisabled(() -> !(world.getSector() != null && world.getSector().currentMission().hasMessage()));
        table.visible(() -> !((world.getSector() == null && state.mode.disableWaves) || !state.mode.showMission || (world.getSector() != null && world.getSector().complete)));
    }

    private void addPlayButton(Table table){
        table.right().addImageButton("icon-play", "right", 30f, () -> {
            if(Net.client() && players[0].isAdmin){
                Call.onAdminRequest(players[0], AdminAction.wave);
            }else{
                state.wavetime = 0f;
            }
        }).growY().fillX().right().width(40f).update(l -> {
            boolean vis = state.mode.disableWaveTimer && state.mode != GameMode.SiegeMode &&
                    ((Net.server() || players[0].isAdmin) || !Net.active());
            boolean paused = state.is(State.paused) || !vis;

            l.getStyle().imageUp = Core.scene.getSkin().getDrawable(vis ? "icon-play" : "clear");
            l.touchable = !paused ? Touchable.enabled : Touchable.disabled;
        }).visible(() -> state.mode.disableWaveTimer && state.mode != GameMode.SiegeMode &&
                ((Net.server() || players[0].isAdmin) || !Net.active()) && unitGroups[state.enemyTeam.ordinal()].size() == 0);
    }
}

