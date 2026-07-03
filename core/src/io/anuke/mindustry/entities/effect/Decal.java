package io.anuke.mindustry.entities.effect;

import arc.graphics.Color;
import io.anuke.mindustry.entities.traits.BelowLiquidTrait;
import arc.entities.EntityGroup;
import arc.entities.impl.TimedEntity;
import arc.entities.trait.DrawTrait;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.groundEffectGroup;

/**
 * Class for creating block rubble on the ground.
 */
public abstract class Decal extends TimedEntity implements BelowLiquidTrait, DrawTrait{
    private static final Color defaultColor = Color.valueOf("52504e");
    protected Color color = defaultColor;

    @Override
    public float lifetime(){
        return 8200f;
    }

    @Override
    public void draw(){
        Draw.color(color.r, color.g, color.b, 1f - Mathf.curve(fin(), 0.98f));
        drawDecal();
        Draw.color();
    }

    @Override
    public EntityGroup targetGroup(){
        return groundEffectGroup;
    }

    abstract void drawDecal();
}
