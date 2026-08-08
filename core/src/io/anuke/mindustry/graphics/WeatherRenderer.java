package io.anuke.mindustry.graphics;

import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.game.EventType.WorldLoadEvent;
import io.anuke.mindustry.io.MapIO;
import io.anuke.mindustry.maps.Map;
import io.anuke.mindustry.maps.MapTileData;
import io.anuke.mindustry.maps.MapTileData.TileDataMarker;
import io.anuke.mindustry.type.RainWeather;
import io.anuke.mindustry.type.SnowWeather;
import io.anuke.mindustry.type.Weather;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.Floor;
import io.anuke.mindustry.world.blocks.OreBlock;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.core.Settings;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Mathf;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;

import static io.anuke.mindustry.Vars.content;
import static io.anuke.mindustry.Vars.headless;
import static io.anuke.mindustry.Vars.state;
import static io.anuke.mindustry.Vars.world;

/**
 * Manages and renders weather effects.
 * Weather runs in an automatic cycle while playing; it can also be forced on via a map tag.
 * Each weather can be customized: whether it always plays, the min/max time between events
 * and the duration of each event. A weather is picked automatically from the map terrain.
 * Adapted from modern Mindustry's weather system.
 */
public class WeatherRenderer{
    private final Weather[] weathers;
    private final ObjectMap<Weather, Boolean> always = new ObjectMap<>();
    private final ObjectMap<Weather, Integer> minFreqs = new ObjectMap<>();
    private final ObjectMap<Weather, Integer> maxFreqs = new ObjectMap<>();
    private final ObjectMap<Weather, Integer> durations = new ObjectMap<>();
    private Weather active;
    private float intensity = 1f;
    private float opacity;
    private float duration;
    private float cooldown;
    private boolean forced;
    private boolean autoWeather = true;
    private int selected = -1;
    private boolean dayNight = true;
    private float cycleDuration = 24f;
    private float cycleTime;
    private float cycleDarkness;
    private float targetDarkness = 1f;

    public WeatherRenderer(){
        weathers = new Weather[]{new RainWeather("rain"), new SnowWeather("snow")};
        for(Weather weather : weathers){
            weather.init();
            weather.load();
        }
        cooldown = Mathf.random(5600f, 10600f);

        Events.on(WorldLoadEvent.class, event -> autoSelect());
    }

    /**Advances the weather cycle. Called every game tick while playing. */
    public void update(){
        updateDayNight();

        if(active != null){
            if(!isAlways(active)){
                duration -= Timers.delta();
            }
            opacity = Mathf.lerpDelta(opacity, 1f, 0.01f);

            if(duration < 0 && !isAlways(active)){
                active = null;
                cooldown = randomCooldown();
            }

            if(active != null){
                //fade out near the end of the weather event
                float fadeTime = 300f;
                if(duration < fadeTime && !isAlways(active)){
                    opacity = Math.min(opacity, Math.max(duration / fadeTime, 0f));
                }
                active.opacity = opacity;
            }
        }else if(!forced){
            if(selected >= 0){
                if(isAlways(weathers[selected])){
                    start(selected);
                }else{
                    cooldown -= Timers.delta();
                    if(cooldown <= 0){
                        start(selected);
                    }
                }
            }
        }
    }

    private void start(int index){
        active = weathers[index];
        intensity = Mathf.random(0.6f, 1f);
        opacity = 0f;
        duration = getDuration(active) * 3600f;
    }

    /**Advances the day/night cycle. Night falls once per cycle, with each night reaching a random
     * darkness between 75% and 100%. */ // I got lazy to add moonlight states
    private void updateDayNight(){
        if(!dayNight){
            cycleDarkness = 0f;
            return;
        }
        float cycleTicks = cycleDuration * 3600f;
        cycleTime += Timers.delta();
        if(cycleTime >= cycleTicks){
            cycleTime -= cycleTicks;
            targetDarkness = Mathf.random(0.75f, 1f);
        }
        float phase = cycleTime / cycleTicks;
        float night = phase >= 0.5f ? MathUtils.sinDeg((phase - 0.5f) * 360f) : 0f;
        cycleDarkness = targetDarkness * night;
    }

    public boolean isDayNight(){
        return dayNight;
    }

    public void setDayNight(boolean value){
        dayNight = value;
        cycleTime = 0f;
        targetDarkness = Mathf.random(0.75f, 1f);
        cycleDarkness = 0f;
    }

    /**Day/night cycle length in minutes. */
    public float getCycleDuration(){
        return cycleDuration;
    }

    public void setCycleDuration(float value){
        cycleDuration = value;
    }

    /**Current day/night darkness level, from 0 (day) to 1 (pitch black night). */
    public float cycleDarkness(){
        return cycleDarkness;
    }

    /**Selects which weather will play from now on. -1 disables weather entirely.
     * The weather starts playing after the configured cooldown. */
    public void select(int index){
        if(forced) return;
        selected = index;
        active = null;
        cooldown = randomCooldown();
        if(index >= 0 && isAlways(weathers[index])){
            start(index);
        }
    }

    /**Immediately starts playing the given weather, or stops all weather if -1. */
    public void play(int index){
        if(forced) return;
        selected = index;
        if(index >= 0){
            start(index);
        }else{
            active = null;
            cooldown = randomCooldown();
        }
    }

    public int selected(){
        return selected;
    }

    public Weather[] weathers(){
        return weathers;
    }

    public boolean isAlways(Weather weather){
        return always.get(weather, false);
    }

    public void setAlways(Weather weather, boolean value){
        always.put(weather, value);
    }

    /**Minimum time between weather events, in minutes. */
    public float getMinFrequency(Weather weather){
        return minFreqs.get(weather, 20);
    }

    public void setMinFrequency(Weather weather, int value){
        minFreqs.put(weather, value);
    }

    /**Maximum time between weather events, in minutes. */
    public float getMaxFrequency(Weather weather){
        return maxFreqs.get(weather, 60);
    }

    public void setMaxFrequency(Weather weather, int value){
        maxFreqs.put(weather, value);
    }

    /**How long a weather event lasts, in minutes. */
    public float getDuration(Weather weather){
        return durations.get(weather, 10);
    }

    public void setDuration(Weather weather, int value){
        durations.put(weather, value);
    }

    /**Resets all weather settings and state back to defaults. Called when setting up a new game. */
    public void reset(){
        always.clear();
        minFreqs.clear();
        maxFreqs.clear();
        durations.clear();
        autoWeather = true;
        selected = -1;
        active = null;
        forced = false;
        opacity = 0f;
        cooldown = Mathf.random(6000f, 12000f);
        dayNight = true;
        cycleTime = 0f;
        cycleDarkness = 0f;
        targetDarkness = 1f;
    }

    public boolean isAutoWeather(){
        return autoWeather;
    }

    public void setAutoWeather(boolean value){
        autoWeather = value;
    }

    /**Automatically selects the weather based on the terrain of the current map.
     * If any weather is set to always play, it takes priority over the terrain.*/ //stupi logic indeed
    public void autoSelect(){
        if(!forced){
            int always = pickAlwaysWeather();
            if(autoWeather){
                select(always >= 0 ? always : selectWeatherForTerrain());
            }else{
                select(always);
            }
        }
    }

    /**Automatically selects the weather based on the terrain of the given map, before the world is loaded.
     * Clears any forced weather from a previous game; the map's own rain tag will re-apply when it loads.*/
    public void autoSelect(Map map){
        forced = false;
        int always = pickAlwaysWeather();
        if(autoWeather){
            select(always >= 0 ? always : selectWeatherForTerrain(map));
        }else{
            select(always);
        }
    }

    /**Returns the index of the first weather set to always play, or -1 if none. */
    private int pickAlwaysWeather(){
        for(int i = 0; i < weathers.length; i++){
            if(isAlways(weathers[i])) return i;
        }
        return -1;
    }
    public int selectWeatherForTerrain(){
        return selectWeatherForTerrain(new WorldTileProvider(){
            @Override
            public Block floor(int x, int y){
                Tile tile = world.rawTile(x, y);
                return tile == null ? null : tile.floor();
            }

            @Override
            public int width(){
                return world.width();
            }

            @Override
            public int height(){
                return world.height();
            }
        });
    }
    public int selectWeatherForTerrain(Map map){
        try{
            MapTileData data = MapIO.readTileData(map, true);
            TileDataMarker marker = data.newDataMarker();
            data.position(0, 0);

            return selectWeatherForTerrain(new MapTileDataProvider(data, marker));
        }catch(Exception e){
            Log.err(e);
            return -1;
        }
    }

    /**Counts the terrain and picks the matching weather. */
    private int selectWeatherForTerrain(WorldTileProvider provider){
        int ice = 0, snow = 0, sand = 0, grass = 0, water = 0, total = 0;

        for(int x = 0; x < provider.width(); x++){
            for(int y = 0; y < provider.height(); y++){
                Block block = provider.floor(x, y);
                if(block == null) continue;
                total++;

                Floor floor;
                if(block instanceof OreBlock){
                    floor = ((OreBlock) block).base;
                }else if(block instanceof Floor){
                    floor = (Floor) block;
                }else{
                    continue;
                }

                if(floor == Blocks.ice){
                    ice++;
                }else if(floor == Blocks.snow){
                    snow++;
                }else if(floor == Blocks.sand || floor == Blocks.infectedSand){
                    sand++;
                }else if(floor == Blocks.grass || floor == Blocks.infectedGrass){
                    grass++;
                }else if(floor == Blocks.water || floor == Blocks.deepwater ||
                        floor == Blocks.infectedWater || floor == Blocks.infectedDeepWater){
                    water++;
                }
            }
        }

        if(total == 0) return -1;

        if(sand > total * 0.6f) return -1;

        int cold = ice + snow;
        int lush = grass + water;

        if(cold > sand && cold > grass && cold > water) return 1;
        if(lush > sand && lush > ice && lush > snow) return 0;

        return -1;
    }

    //provides floor blocks by tile coordinates, for terrain counting
    private interface WorldTileProvider{
        Block floor(int x, int y);
        int width();
        int height();
    }

    //reads floors from a map file's tile data
    private class MapTileDataProvider implements WorldTileProvider{
        private final MapTileData data;
        private final TileDataMarker marker;

        MapTileDataProvider(MapTileData data, TileDataMarker marker){
            this.data = data;
            this.marker = marker;
        }

        @Override
        public Block floor(int x, int y){
            data.read(marker);
            return content.block(marker.floor);
        }

        @Override
        public int width(){
            return data.width();
        }

        @Override
        public int height(){
            return data.height();
        }
    }

    private float randomCooldown(){
        if(selected < 0) return Mathf.random(6000f, 12000f);
        return Mathf.random(getMinFrequency(weathers[selected]), getMaxFrequency(weathers[selected])) * 3600f;
    }

    /**Forces weather on or off, used when loading a map from a tag. */
    public void setRain(boolean rain){
        if(forced == rain) return;
        forced = rain;

        if(forced){
            active = weathers[0];
            intensity = 1f;
            opacity = 0f;
            duration = Float.MAX_VALUE;
        }else if(active != null){
            active = null;
            cooldown = Mathf.random(6000f, 12000f);
        }
    }

    public boolean isForced(){
        return forced;
    }

    /**Draws weather effects on top of the world. */
    public void drawOver(){
        if(!canDraw()) return;
        active.drawOver(opacity * intensity);
    }

    /**Draws weather effects under units, e.g. splashes. */
    public void drawUnder(){
        if(!canDraw()) return;
        active.drawUnder(opacity * intensity);
    }

    private boolean canDraw(){
        return !headless && active != null && opacity > 0.001f
                && Settings.getBool("showweather", true)
                && (state.is(State.playing) || state.is(State.paused));
    }
}
