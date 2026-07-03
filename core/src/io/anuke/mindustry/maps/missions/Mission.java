package io.anuke.mindustry.maps.missions;

import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Bundles;
import arc.util.Timers;
import io.anuke.mindustry.content.blocks.StorageBlocks;
import io.anuke.mindustry.game.GameMode;
import io.anuke.mindustry.game.SpawnGroup;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.game.UnlockableContent;
import io.anuke.mindustry.maps.Sector;
import io.anuke.mindustry.maps.generation.Generation;
import arc.util.Time;
import arc.scene.ui.layout.Table;
import arc.util.Strings;

import static io.anuke.mindustry.Vars.*;

public abstract class Mission{
    private String extraMessage;
    private boolean showComplete = true;

    public abstract boolean isComplete();

    /**Returns the string that is displayed in-game near the menu.*/
    public abstract String displayString();

    /**Returns the info string displayed in the sector dialog (menu)*/
    public String menuDisplayString(){
        return displayString();
    }

    public String getIcon(){
        return "icon-mission-defense";
    }

    public boolean isInfectable(){
        return false;
    }

    public boolean isInfected(){
        return false;
    }

    public GameMode getMode(){
        return GameMode.noWaves;
    }

    /**Sets the message displayed on mission begin. Returns this mission for chaining.*/
    public Mission setMessage(String message){
        this.extraMessage = message;
        return this;
    }

    public Mission setShowComplete(boolean complete){
        this.showComplete = complete;
        return this;
    }

    /**Called when a specified piece of content is 'used' by a block.*/
    public void onContentUsed(UnlockableContent content){

    }

    /**Draw mission overlay.*/
    public void drawOverlay(){

    }

    public void update(){

    }

    public void reset(){

    }

    /**Shows the unique sector message.*/
    public void showMessage(){
        if(!headless && extraMessage != null){
            ui.hudfrag.showTextDialog(extraMessage);
        }
    }

    public boolean hasMessage(){
        return extraMessage != null;
    }

    public void onBegin(){
        Timers.runTask(60f, this::showMessage);
    }

    public void onComplete(){
        if(showComplete && !headless){
            threads.runGraphics(() -> ui.hudfrag.showToast("[LIGHT_GRAY]"+menuDisplayString() + ":\n" + Bundles.get("text.mission.complete")));
        }
    }

    public void display(Table table){
        table.add(displayString());
    }

    public Seq<SpawnGroup> getWaves(Sector sector){
        return new Seq<>();
    }

    public Seq<Point2> getSpawnPoints(Generation gen){
        return Seq.with();
    }

    public void generate(Generation gen){}
}
