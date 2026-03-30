package io.anuke.mindustry.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import io.anuke.mindustry.Vars;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.core.Settings;
import io.anuke.ucore.modules.Module;
import io.anuke.ucore.util.Mathf;

public class SoundController extends Module{
    private static final long defaultMinInterval = 100L;

    private final ObjectMap<String, Sound> sounds = new ObjectMap<>();
    private final ObjectMap<String, Array<Sound>> groups = new ObjectMap<>();
    private final ObjectMap<Sound, Long> lastPlayed = new ObjectMap<>();
    private final ObjectMap<Sound, Long> minIntervals = new ObjectMap<>();

    private float falloff = 9000f;

    public SoundController(){
        if(Vars.headless) return;
        Settings.defaults("sfxvol", 10);
        Settings.defaults("mutesound", false);
        loadDirectory("sounds");
    }

    public void loadDirectory(String path){
        FileHandle dir = Gdx.files.internal(path);
        if(!dir.exists() || !dir.isDirectory()) return;

        for(FileHandle file : dir.list()){
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

        Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
        sounds.put(name, sound);
        return sound;
    }

    public void createGroup(String name, String... soundNames){
        Array<Sound> list = new Array<>();
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
            lastPlayed.put(sound, TimeUtils.millis());
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
            lastPlayed.put(sound, TimeUtils.millis());
        }
        return id;
    }

    public long playRandom(String group){
        return playRandom(group, 1f);
    }

    public long playRandom(String group, float volume){
        Array<Sound> list = groups.get(group);
        if(list == null || list.size == 0) return -1L;
        return play(list.random(), volume);
    }

    public long atRandom(String group, float x, float y, float pitch, float volume){
        Array<Sound> list = groups.get(group);
        if(list == null || list.size == 0) return -1L;
        return at(list.random(), x, y, pitch, volume);
    }

    private boolean canPlay(Sound sound){
        if(Vars.headless || Settings.getBool("mutesound")) return false;

        long interval = minIntervals.get(sound, defaultMinInterval);
        long last = lastPlayed.get(sound, 0L);
        return TimeUtils.timeSinceMillis(last) >= interval;
    }

    private float getVolume(){
        return Settings.getInt("sfxvol", 10) / 10f;
    }

    private float calcPan(float x){
        if(Core.camera == null) return 0f;
        return Mathf.clamp((x - Core.camera.position.x) / (Core.camera.viewportWidth / 2f), -0.9f, 0.9f);
    }

    private float calcVolume(float x, float y){
        if(Core.camera == null) return getVolume();
        float dst2 = (x - Core.camera.position.x) * (x - Core.camera.position.x) + (y - Core.camera.position.y) * (y - Core.camera.position.y);
        return Mathf.clamp(1f / Math.max(dst2 / falloff, 1f)) * getVolume();
    }

    @Override
    public void dispose(){
        for(Sound sound : sounds.values()){
            sound.dispose();
        }
        sounds.clear();
        groups.clear();
        lastPlayed.clear();
        minIntervals.clear();
    }
}
