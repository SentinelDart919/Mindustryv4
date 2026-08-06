package io.anuke.mindustry.world.blocks;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.mindustry.content.fx.TreeFx;
import io.anuke.mindustry.graphics.DrawPseudo3D;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

public class DeadTree extends Prop{
    public float shadowOffset = -4f;
    protected TextureRegion shadow;

    public DeadTree(String name){
        super(name);
        solid = true;
        breakable = true;
        layer = Layer.tree;
    }

    @Override
    public void onDestroyed(Tile tile){
        super.onDestroyed(tile);

        Effects.effect(TreeFx.stumpBreakEffect(40f, 5, variants, regions, 2f, 4f), tile.worldx(), tile.worldy());
    }

    @Override
    public void draw(Tile tile){
    }

    @Override
    public void drawLayer(Tile tile){
        float x = tile.worldx(), y = tile.worldy();
        float rot = Mathf.randomSeed(tile.id(), 0, 4) * 90 + Mathf.sin(Timers.time() + x, 50f, 0.5f)
                + Mathf.sin(Timers.time() - y, 65f, 0.9f) + Mathf.sin(Timers.time() + y - x, 85f, 0.9f);
        float w = region.getRegionWidth(), h = region.getRegionHeight();
        float scl = 30f, mag = 0.2f;

        float height = 0.05f;
        float dx = DrawPseudo3D.xOffset(x, height);
        float dy = DrawPseudo3D.yOffset(y, height);

        Draw.rectv(region, x + dx, y + dy, w, h, rot, vec -> vec.add(
                Mathf.sin(vec.y * 3 + Timers.time(), scl, mag) + Mathf.sin(vec.x * 3 - Timers.time(), 70, 0.8f),
                Mathf.cos(vec.x * 3 + Timers.time() + 8, scl + 6f, mag * 1.1f) + Mathf.sin(vec.y * 3 - Timers.time(), 50, 0.2f)
        ));
    }

    @Override
    public void drawShadow(Tile tile){
        float rot = Mathf.randomSeed(tile.id(), 0, 4) * 90;
        Draw.rect(shadow, tile.worldx() + shadowOffset, tile.worldy() + shadowOffset, rot);
    }

    @Override
    public void load(){
        super.load();
        shadow = Draw.region(name + "-shadow");
    }
}
