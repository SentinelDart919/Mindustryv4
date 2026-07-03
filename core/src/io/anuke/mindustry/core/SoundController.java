package io.anuke.mindustry.core;

import arc.modules.Module;

import arc.Core;
import arc.audio.Sound;
import arc.files.Fi;
import arc.struct.Seq;
import arc.struct.ObjectMap;
import arc.struct.ObjectIntMap;
import arc.struct.ObjectSet;
import arc.util.Time;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.core.GameState;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import arc.Core;
import arc.ApplicationListener;
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.tilesize;
import static io.anuke.mindustry.Vars.world;

public class SoundController extends Module{
    private static final long defaultMinInterval = 100L;

    private final ObjectMap<String, Sound> sounds = new ObjectMap<>();
    private final ObjectMap<String, Seq<Sound>> groups = new ObjectMap<>();
    private final ObjectMap<Sound, Long> lastPlayed = new ObjectMap<>();
    private final ObjectMap<Sound, Integer> priorities = new ObjectMap<>();
    private final ObjectMap<Sound, Long> minIntervals = new ObjectMap<>();
    private final ObjectSet<TileEntity> ambientEntities = new ObjectSet<>();
    private final ObjectSet<TileEntity> nextAmbientEntities = new ObjectSet<>();
    private final Seq<Tile> ambientTiles = new Seq<>();
    private final ObjectIntMap<Block> ambientCounts = new ObjectIntMap<>();
    private final Seq<PriorityEntry> priorityEntries = new Seq<>();
    private int ambientSoundBudget = 6;
    private int prioritySoundBudget = 8;
    private long priorityWindow = 120L;

    private float falloff = 9000f;

    public SoundController(){
        if(Vars.headless) return;
        Core.settings.defaults("sfxvol", 10);
        Core.settings.defaults("mutesound", false);
        loadDirectory("sounds");
    }

    public void loadDirectory(String path){
        Fi dir = Core.files.internal(path);
        if(!dir.exists() || !dir.isDirectory()) return;

        for(Fi file : dir.list()){
            if(file.isDirectory()){
                loadDirectory(file.path());
                continue;
            }

            String ext = file.extension().toLowerCase();
            if(!ext.equals("ogg") && !ext.equals("wav") && !ext.equals("mp3")) continue;

            load(file.nameWithoutExtension(), file.path());
        }
    }

    public Sound load(String name, String path){
        Sound existing = sounds.get(name);
        if(existing != null){
            existing.dispose();
        }

        Sound sound = Core.audio.newSound(Core.files.internal(path));
        sounds.put(name, sound);
        return sound;
    }

    public void createGroup(String name, String... soundNames){
        Seq<Sound> list = new Seq<>();
        for(String soundName : soundNames){
            Sound sound = sounds.get(soundName);
            if(sound != null){
                list.add(sound);
            }
        }

        groups.put(name, list);
    }

    public Sound get(String name){
        return sounds.get(name);
    }

    public void setFalloff(float falloff){
        this.falloff = falloff;
    }

    public void setPriority(String name, Integer priority){
        Sound sound = sounds.get(name);
        if(sound != null){
            setPriority(sound, priority);
        }
    }

    public void setPriority(Sound sound, Integer priority){
        if(sound == null) return;

        if(priority == null){
            priorities.remove(sound);
        }else{
            priorities.put(sound, priority);
        }
    }

    public Integer getPriority(Sound sound){
        return sound == null ? null : priorities.get(sound);
    }

    public void setAmbientSoundBudget(int amount){
        ambientSoundBudget = Math.max(0, amount);
    }

    public void setPrioritySoundBudget(int amount){
        prioritySoundBudget = Math.max(0, amount);
    }

    public void setPriorityWindow(long millis){
        priorityWindow = Math.max(0L, millis);
    }

    public void setMinInterval(String name, long interval){
        Sound sound = sounds.get(name);
        if(sound != null){
            minIntervals.put(sound, interval);
        }
    }

    public long play(String name){
        return play(name, 1f);
    }

    public long play(String name, float volume){
        Sound sound = sounds.get(name);
        return sound == null ? -1L : play(sound, volume);
    }

    public long play(String name, float volume, float pitch, float pan){
        Sound sound = sounds.get(name);
        return sound == null ? -1L : play(sound, volume, pitch, pan);
    }

    public long play(Sound sound, float volume){
        return play(sound, volume, 1f, 0f);
    }

    public long play(Sound sound, float volume, float pitch, float pan){
        if(sound == null || !canPlay(sound)) return -1L;

        float finalVolume = getVolume() * volume;
        if(finalVolume <= 0.001f) return -1L;

        long id = sound.play(Mathf.clamp(finalVolume, 0f, 1f), pitch, Mathf.clamp(pan, -1f, 1f));
        if(id != -1L){
            lastPlayed.put(sound, Time.millis());
            registerPriority(sound);
        }
        return id;
    }

    public long at(String name, float x, float y){
        return at(name, x, y, 1f, 1f);
    }

    public long at(String name, float x, float y, float pitch, float volume){
        Sound sound = sounds.get(name);
        return sound == null ? -1L : at(sound, x, y, pitch, volume);
    }

    public long at(Sound sound, float x, float y, float pitch, float volume){
        if(sound == null || !canPlay(sound)) return -1L;

        float finalVolume = calcVolume(x, y) * volume;
        if(finalVolume <= 0.01f) return -1L;

        long id = sound.play(Mathf.clamp(finalVolume, 0f, 1f), pitch, calcPan(x));
        if(id != -1L){
            lastPlayed.put(sound, Time.millis());
            registerPriority(sound);
        }
        return id;
    }

    public long updateLoop(Sound sound, long id, float x, float y, float volume){
        if(sound == null || Vars.headless){
            return stopLoop(sound, id);
        }

        if(id == -1L){
            id = sound.loop(0f, 1f, calcPan(x));
            if(id == -1L) return -1L;
            sound.setLooping(id, true);
        }

        float finalVolume = calcVolume(x, y) * volume;
        if(Core.settings.getBool("mutesound") || finalVolume <= 0.001f){
            sound.pause(id);
        }else{
            sound.resume(id);
            sound.setPan(id, calcPan(x), Mathf.clamp(finalVolume, 0f, 1f));
        }

        return id;
    }

    @Override
    public void update(){
        if(Vars.headless || Core.camera == null) return;

        if(Vars.state.is(GameState.State.menu)){
            for(TileEntity entity : ambientEntities){
                if(entity != null && entity.tile != null){
                    entity.tile.block().stopAmbientSound(entity.tile);
                }
            }
            ambientEntities.clear();
            return;
        }

        nextAmbientEntities.clear();
        ambientTiles.clear();
        ambientCounts.clear();

        float worldRange = Math.max(Core.camera.width, Core.camera.height) * 1.5f;
        int tileRange = Math.max(1, (int)(worldRange / tilesize) + 2);
        int centerX = Mathf.scl(Core.camera.position.x, tilesize);
        int centerY = Mathf.scl(Core.camera.position.y, tilesize);

        int minx = Math.max(0, centerX - tileRange);
        int miny = Math.max(0, centerY - tileRange);
        int maxx = Math.min(world.width() - 1, centerX + tileRange);
        int maxy = Math.min(world.height() - 1, centerY + tileRange);

        for(int x = minx; x <= maxx; x++){
            for(int y = miny; y <= maxy; y++){
                Tile tile = world.rawTile(x, y);
                if(tile == null || tile.entity == null || tile.block().ambientSound == null) continue;

                ambientTiles.add(tile);
            }
        }

        ambientTiles.sort((a, b) -> {
            int apr = ambientPriority(a);
            int bpr = ambientPriority(b);
            if(apr != bpr) return Integer.compare(bpr, apr);
            return Float.compare(dst2(a), dst2(b));
        });

        int totalAmbient = 0;
        for(Tile tile : ambientTiles){
            Block block = tile.block();
            TileEntity entity = tile.entity;
            int count = ambientCounts.get(block, 0);
            boolean underGlobalBudget = ambientSoundBudget <= 0 || totalAmbient < ambientSoundBudget;
            boolean shouldPlay = block.shouldPlayAmbientSound(tile);

            if(!shouldPlay || !underGlobalBudget || (block.ambientSoundLimit > 0 && count >= block.ambientSoundLimit)){
                block.updateAmbientSound(tile, false);
            }else{
                block.updateAmbientSound(tile, true);
                ambientCounts.put(block, count + 1);
                totalAmbient++;
            }

            if(entity.ambientSoundId != -1L || entity.ambientSoundFade > 0.001f){
                nextAmbientEntities.add(entity);
            }
        }

        for(TileEntity entity : ambientEntities){
            if(entity == null || entity.tile == null || nextAmbientEntities.contains(entity)) continue;

            entity.tile.block().updateAmbientSound(entity.tile, false);
            if(entity.ambientSoundId != -1L || entity.ambientSoundFade > 0.001f){
                nextAmbientEntities.add(entity);
            }
        }

        ambientEntities.clear();
        ambientEntities.addAll(nextAmbientEntities);
    }

    public long stopLoop(Sound sound, long id){
        if(sound != null && id != -1L){
            sound.stop(id);
        }
        return -1L;
    }

    public long playRandom(String group){
        return playRandom(group, 1f);
    }

    public long playRandom(String group, float volume){
        Seq<Sound> list = groups.get(group);
        if(list == null || list.size == 0) return -1L;
        return play(list.random(), volume);
    }

    public long playRandom(String group, float volume, float pitch, float pan){
        Seq<Sound> list = groups.get(group);
        if(list == null || list.size == 0) return -1L;
        return play(list.random(), volume, pitch, pan);
    }

    public long atRandom(String group, float x, float y, float pitch, float volume){
        Seq<Sound> list = groups.get(group);
        if(list == null || list.size == 0) return -1L;
        return at(list.random(), x, y, pitch, volume);
    }

    public Sound random(String group){
        Seq<Sound> list = groups.get(group);
        return list == null || list.size == 0 ? null : list.random();
    }

    private boolean canPlay(Sound sound){
        if(Vars.headless || Core.settings.getBool("mutesound")) return false;

        long interval = minIntervals.get(sound, defaultMinInterval);
        long last = lastPlayed.get(sound, 0L);
        return Time.timeSinceMillis(last) >= interval && canPlayPriority(sound);
    }

    private boolean canPlayPriority(Sound sound){
        Integer priority = getPriority(sound);
        if(priority == null || prioritySoundBudget <= 0) return true;

        cleanupPriorityEntries();
        if(priorityEntries.size < prioritySoundBudget) return true;

        int lowest = Integer.MAX_VALUE;
        for(PriorityEntry entry : priorityEntries){
            lowest = Math.min(lowest, entry.priority);
        }

        return priority > lowest;
    }

    private void registerPriority(Sound sound){
        Integer priority = getPriority(sound);
        if(priority == null || prioritySoundBudget <= 0) return;

        cleanupPriorityEntries();
        if(priorityEntries.size >= prioritySoundBudget){
            int lowestIndex = -1;
            int lowestPriority = Integer.MAX_VALUE;
            for(int i = 0; i < priorityEntries.size; i++){
                PriorityEntry entry = priorityEntries.get(i);
                if(entry.priority < lowestPriority){
                    lowestPriority = entry.priority;
                    lowestIndex = i;
                }
            }

            if(lowestIndex != -1 && priority > lowestPriority){
                priorityEntries.removeIndex(lowestIndex);
            }else if(lowestIndex != -1){
                return;
            }
        }

        priorityEntries.add(new PriorityEntry(Time.millis(), priority));
    }

    private void cleanupPriorityEntries(){
        long now = Time.millis();
        for(int i = priorityEntries.size - 1; i >= 0; i--){
            if(now - priorityEntries.get(i).time > priorityWindow){
                priorityEntries.removeIndex(i);
            }
        }
    }

    private float getVolume(){
        return Core.settings.getInt("sfxvol", 10) / 10f;
    }

    private float calcPan(float x){
        if(Core.camera == null) return 0f;
        return Mathf.clamp((x - Core.camera.position.x) / (Core.camera.width / 2f), -0.9f, 0.9f);
    }

    private float calcVolume(float x, float y){
        if(Core.camera == null) return getVolume();
        float dst2 = (x - Core.camera.position.x) * (x - Core.camera.position.x) + (y - Core.camera.position.y) * (y - Core.camera.position.y);
        return Mathf.clamp(1f / Math.max(dst2 / falloff, 1f)) * getVolume();
    }

    private float dst2(Tile tile){
        float dx = tile.drawx() - Core.camera.position.x;
        float dy = tile.drawy() - Core.camera.position.y;
        return dx * dx + dy * dy;
    }

    private int ambientPriority(Tile tile){
        Integer priority = getPriority(tile.block().ambientSound);
        return priority == null ? 0 : priority;
    }

    private static class PriorityEntry{
        final long time;
        final int priority;

        PriorityEntry(long time, int priority){
            this.time = time;
            this.priority = priority;
        }
    }

    @Override
    public void dispose(){
        for(Sound sound : sounds.values()){
            sound.dispose();
        }
        sounds.clear();
        groups.clear();
        lastPlayed.clear();
        priorities.clear();
        minIntervals.clear();
        priorityEntries.clear();
    }
}

