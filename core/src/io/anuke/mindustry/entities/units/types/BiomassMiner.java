package io.anuke.mindustry.entities.units.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

public class BiomassMiner extends DroneMiner{
    @Override
    public void drawStats(){
        float hf = healthf();
        float frequency = 1f + (1f - hf) * 3f;
        float amplitude = 0.1f + (1f - hf) * 0.15f;

        float scale = 1f + Mathf.sin(Timers.time() * frequency, 2f, amplitude);

        Draw.color(Color.BLACK, team.color, hf + Mathf.absin(Timers.time(), hf * 5f, 1f - hf));
        Draw.alpha(hitTime);
        Draw.rect(getPowerCellRegion(), x, y,
                getPowerCellRegion().getRegionWidth() * scale,
                getPowerCellRegion().getRegionHeight() * scale,
                rotation - 90);
        Draw.color();
    }
    @Override
    public void drawOver(){
        trail.draw(Color.valueOf("871e1e"), 3f);
        drawMining(this);
    }

    @Override
    public TextureRegion getPowerCellRegion(){
        return Draw.region("biomass-heart");
    }
}
