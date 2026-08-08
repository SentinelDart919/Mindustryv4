package io.anuke.mindustry.type;

import com.badlogic.gdx.math.RandomXS128;
import io.anuke.mindustry.game.Content;

/**
 * Base class for weather content.
 * Weather is rendered and managed by {@link io.anuke.mindustry.graphics.WeatherRenderer}.
 * Adapted from modern Mindustry's Weather class.
 */
public abstract class Weather extends Content{
    /**Shared random used for rendering. Seeded every frame for a consistent animation.*/
    public static final RandomXS128 rand = new RandomXS128();
    /**Name of this weather, e.g. "rain".*/
    public final String name;
    /**Current opacity of this weather, managed by the renderer.*/
    public float opacity;

    public Weather(String name){
        this.name = name;
    }

    @Override
    public ContentType getContentType(){
        return ContentType.weather;
    }

    /**Called every game tick while this weather is active.*/
    public void update(float delta){
    }

    /**Draws this weather on top of the world, e.g. rain streaks.*/
    public void drawOver(float alpha){
    }

    /**Draws weather effects that appear under units, e.g. splashes.*/
    public void drawUnder(float alpha){
    }
}
