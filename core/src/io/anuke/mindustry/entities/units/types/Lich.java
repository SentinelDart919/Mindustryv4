package io.anuke.mindustry.entities.units.types;

import com.badlogic.gdx.graphics.Color;
import io.anuke.mindustry.entities.units.FlyingUnit;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.ucore.util.Angles;

public class Lich extends FlyingUnit{
    public Lich() {
        customTrail = true;
    }
    @Override
    public void update(){
        super.update();
        float back = -21f;
        float side = 0f;
        trail.update(
                x + Angles.trnsx(rotation, back, side),
                y + Angles.trnsy(rotation, back, side)
        );
    }
    @Override
    public void drawOver(){
        trail.draw(Palette.lighterOrange, 8f);
    }
}
