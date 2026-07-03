package io.anuke.mindustry.game;

import arc.Core;
import arc.files.Fi;
import arc.struct.Seq;
import arc.struct.IntSeq;
import arc.struct.IntMap;
import arc.util.Time;
import arc.util.Timers;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.game.EventType.StateChangeEvent;
import io.anuke.mindustry.io.SaveIO;
import io.anuke.mindustry.io.SaveMeta;
import io.anuke.mindustry.maps.Map;
import arc.Events;
import arc.util.Time;
import arc.util.Strings;
import arc.struct.Seq;

import java.io.IOException;

import static io.anuke.mindustry.Vars.*;

public class Saves{
    private int nextSlot;
    private Seq<SaveSlot> saves = new Seq<>();
    private IntMap<SaveSlot> saveMap = new IntMap<>();
    private SaveSlot current;
    private boolean saving;
    private float time;

    private long totalPlaytime;
    private long lastTimestamp;

    public Saves(){
        Events.on(StateChangeEvent.class, event -> {
            if(event.to == State.menu){
                threads.run(() -> {
                    totalPlaytime = 0;
                    lastTimestamp = 0;
                    current = null;
                });
            }
        });
    }

    public void load(){
        saves.clear();
        IntSeq slots = Core.settings.getObject("save-slots", IntSeq.class, IntSeq::new);

        for(int i = 0; i < slots.size; i ++){
            int index = slots.get(i);
            if(SaveIO.isSaveValid(index)){
                SaveSlot slot = new SaveSlot(index);
                saves.add(slot);
                saveMap.put(slot.index, slot);
                slot.meta = SaveIO.getData(index);
                nextSlot = Math.max(index + 1, nextSlot);
            }
        }
    }

    public SaveSlot getCurrent(){
        return current;
    }

    public void update(){
        SaveSlot current = this.current;

        if(current != null && !state.is(State.menu)
            && !(state.isPaused() && ui.hasDialog())){
            if(lastTimestamp != 0){
                totalPlaytime += Time.timeSinceMillis(lastTimestamp);
            }
            lastTimestamp = Time.millis();
        }

        if(!state.is(State.menu) && !state.gameOver && current != null && current.isAutosave()){
            time += Timers.delta();
            if(time > Core.settings.getInt("saveinterval") * 60){
                saving = true;

                Timers.runTask(2f, () -> {
                    try{
                        current.save();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    saving = false;
                });

                time = 0;
            }
        }else{
            time = 0;
        }
    }

    public long getTotalPlaytime(){
        return totalPlaytime;
    }

    public void resetSave(){
        current = null;
    }

    public boolean isSaving(){
        return saving;
    }

    public SaveSlot addSave(String name){
        SaveSlot slot = new SaveSlot(nextSlot);
        nextSlot++;
        slot.setName(name);
        saves.add(slot);
        saveMap.put(slot.index, slot);
        slot.save();
        saveSlots();
        return slot;
    }

    public SaveSlot importSave(Fi file) throws IOException{
        SaveSlot slot = new SaveSlot(nextSlot);
        slot.importFile(file);
        nextSlot++;
        slot.setName(file.nameWithoutExtension());
        saves.add(slot);
        saveMap.put(slot.index, slot);
        slot.meta = SaveIO.getData(slot.index);
        current = slot;
        saveSlots();
        return slot;
    }

    public SaveSlot getByID(int id){
        return saveMap.get(id);
    }

    public Seq<SaveSlot> getSaveSlots(){
        return saves;
    }

    private void saveSlots(){
        IntSeq result = new IntSeq(saves.size);
        for(int i = 0; i < saves.size; i++) result.add(saves.get(i).index);

        Core.settings.putObject("save-slots", result);
        Core.settings.save();
    }

    public class SaveSlot{
        public final int index;
        SaveMeta meta;

        public SaveSlot(int index){
            this.index = index;
        }

        public void load(){
            SaveIO.loadFromSlot(index);
            meta = SaveIO.getData(index);
            current = this;
            totalPlaytime = meta.timePlayed;
        }

        public void save(){
            long time = totalPlaytime;

            threads.runGraphics(() -> {
                //Renderer fog needs to be written on graphics thread, but save() can run on logic thread
                //thus, runGraphics is required here
                renderer.fog.writeFog();

                //save on the logic thread
                threads.run(() -> {
                    long prev = totalPlaytime;
                    totalPlaytime = time;

                    SaveIO.saveToSlot(index);
                    launchManager.save();
                    meta = SaveIO.getData(index);
                    if(!state.is(State.menu)){
                        current = this;
                    }

                    totalPlaytime = prev;
                });
            });
        }

        public boolean isHidden(){
            return meta.sector != invalidSector;
        }

        public String getPlayTime(){
            return Strings.formatMillis(current == this ? totalPlaytime : meta.timePlayed);
        }

        public String getDate(){
            return meta.date;
        }

        public Map getMap(){
            return meta.map;
        }

        public String getName(){
            return Core.settings.getString("save-" + index + "-name", "untittled");
        }

        public void setName(String name){
            Core.settings.put("save-" + index + "-name", name);
            Core.settings.save();
        }

        public int getBuild(){
            return meta.build;
        }

        public int getWave(){
            return meta.wave;
        }

        public Difficulty getDifficulty(){
            return meta.difficulty;
        }

        public GameMode getMode(){
            return meta.mode;
        }

        public boolean isAutosave(){
            return Core.settings.getBool("save-" + index + "-autosave", true);
        }

        public void setAutosave(boolean save){
            Core.settings.putBool("save-" + index + "-autosave", save);
            Core.settings.save();
        }

        public void importFile(Fi file) throws IOException{
            try{
                file.copyTo(SaveIO.fileFor(index));
            }catch(Exception e){
                throw new IOException(e);
            }
        }

        public void exportFile(Fi file) throws IOException{
            try{
                if(!file.extension().equals(saveExtension)){
                    file = file.parent().child(file.nameWithoutExtension() + "." + saveExtension);
                }
                SaveIO.fileFor(index).copyTo(file);
            }catch(Exception e){
                throw new IOException(e);
            }
        }

        public void delete(){
            SaveIO.fileFor(index).delete();
            saves.remove(this, true);
            saveMap.remove(index);
            if(this == current){
                current = null;
            }

            saveSlots();
        }
    }
}


