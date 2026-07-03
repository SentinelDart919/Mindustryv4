package io.anuke.mindustry.entities.units.types;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.util.Time;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Timers;

public class BiomassMiner extends DroneMiner{
    @Override
    public void drawStats(){
        float hf = healthf();
        float frequency = 1f + (1f - hf) * 3f;
        float amplitude = 0.1f + (1f - hf) * 0.15f;

        float scale = 1f + Mathf.sin(Timers.time() * frequency, 2f, amplitude);

        Draw.color(Color.black, team.color, hf + Mathf.absin(Timers.time(), hf * 5f, 1f - hf));
        Draw.alpha(hitTime);
        Draw.rect(getPowerCellRegion(), x, y,
                getPowerCellRegion().width * scale,
                getPowerCellRegion().height * scale,
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
        if(type.hitsize > 10f)return Core.atlas.find("biomass-heart");
        else  return Core.atlas.find("small-biomass-heart");
    }
}

