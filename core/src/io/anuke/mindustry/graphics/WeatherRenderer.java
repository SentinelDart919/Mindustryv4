package io.anuke.mindustry.graphics;

import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.type.RainWeather;
import io.anuke.mindustry.type.Weather;
import io.anuke.ucore.core.Settings;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.headless;
import static io.anuke.mindustry.Vars.state;

/**
 * Manages and renders weather effects.
 * Weather runs in an automatic cycle while playing; it can also be forced on via a map tag.
 * Adapted from modern Mindustry's weather system.
 */
public class WeatherRenderer{
    private final Weather[] weathers;
    private Weather active;
    private float intensity = 1f;
    private float opacity;
    private float duration;
    private float cooldown;
    private boolean forced;

    public WeatherRenderer(){
        weathers = new Weather[]{new RainWeather("rain")};
        for(Weather weather : weathers){
            weather.init();
            weather.load();
        }
        cooldown = Mathf.random(5600f, 10600f);
    }

    /**Advances the weather cycle. Called every game tick while playing. */
    public void update(){
        if(active != null){
            duration -= Timers.delta();
            opacity = Mathf.lerpDelta(opacity, 1f, 0.01f);

            if(duration < 0 && !forced){
                active = null;
                cooldown = Mathf.random(6000f, 12000f);
            }

            if(active != null){
                //fade out near the end of the weather event
                float fadeTime = 300f;
                if(duration < fadeTime){
                    opacity = Math.min(opacity, Math.max(duration / fadeTime, 0f));
                }
                active.opacity = opacity;
            }
        }else if(!forced){
            cooldown -= Timers.delta();
            if(cooldown <= 0){
                active = weathers[Mathf.random(0, weathers.length - 1)];
                intensity = Mathf.random(0.6f, 1f);
                duration = Mathf.random(7200f, 14400f);
                opacity = 0f;
            }
        }
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
