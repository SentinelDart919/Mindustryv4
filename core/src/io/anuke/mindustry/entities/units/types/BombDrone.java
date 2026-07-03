package io.anuke.mindustry.entities.units.types;

import arc.graphics.Color;
import io.anuke.mindustry.entities.units.FlyingUnit;
import io.anuke.mindustry.graphics.Palette;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;

import static arc.util.Timers.delta;

public class BombDrone extends FlyingUnit {
    float propRot;

    @Override
    public void update(){
        super.update();
        propRot += 25f * delta();
    }
    @Override
    public void draw(){
        Draw.alpha(hitTime / hitDuration);

        Draw.rect(type.name, x, y, rotation - 90);

        drawItems();

        Draw.alpha(1f);

        drawProp(-3.5f,  3.45f,  propRot) ;
        drawProp(-3.5f,  -3.45f,  propRot);
        drawProp( 3.5f,  3.45f, -propRot);
        drawProp( 3.5f, -3.45f , -propRot);
    }

    void drawProp(float localX, float localY, float spin){
        float wx = x + Angles.trnsx(rotation, localX, localY);
        float wy = y + Angles.trnsy(rotation, localX, localY);

        Draw.alpha(hitTime / hitDuration);
        Draw.color();
        Draw.alpha(0f);
        Draw.rect(type.name + "-propeller", wx, wy, spin);
    }
    @Override
    public void drawOver(){
        trail.draw(Color.black, 0f);
    }
}
