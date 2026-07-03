package io.anuke.mindustry.world.blocks.storage;

import arc.graphics.Color;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.graphics.Shaders;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.entities.TileEntity;
import arc.Graphics;
import arc.util.Time;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.graphics.g2d.TextureRegion;

public class HiveBlock extends CoreBlock {

    public HiveBlock(String name) {
        super(name);
    }

    @Override
    public void update(Tile tile) {
        super.update(tile);
        setAmbientSound("none", 0.09f);
        setBuildPlayerSound("none");

        HiveEntity entity = tile.entity();

        // spawns random biomass items every 1-4 minutes
        entity.biomassTimer += Timers.delta();
        if (entity.biomassTimer >= entity.biomassGoal) {
            entity.biomassTimer = 0;
            entity.biomassGoal = Mathf.random(1f, 4f) * 60f * 60f;
            entity.items.add(Items.corruptedbiomatter, Mathf.random(1, 3));
        }
    }

    @Override
    public void draw(Tile tile) {
        HiveEntity entity = tile.entity();

        float pulse = 1f + Mathf.absin(Timers.time(), 4f, 0.05f);

        Draw.rect(entity.solid ? Core.atlas.find(name) : openRegion, tile.drawx(), tile.drawy(), pulse * size * 8f, pulse * size * 8f);

        Draw.alpha(entity.heat);
        Draw.rect(topRegion, tile.drawx(), tile.drawy(), pulse * size * 8f, pulse * size * 8f);
        Draw.color();

        if (entity.currentUnit != null) {// random ass draw
            float time = entity.time;
            float progress = entity.progress;
            Unit player = entity.currentUnit;
            TextureRegion region = player.getIconRegion();

            Shaders.build.region = region;
            Shaders.build.progress = progress;
            Shaders.build.color.set(Color.valueOf("d30000"));
            Shaders.build.time = -time / 10f;

            Gfx.shader(Shaders.build, false);
            Shaders.build.apply();
            Draw.rect(region, tile.drawx(), tile.drawy());
            Gfx.shader();

            Draw.color(Color.valueOf("d30000"));

            Lines.lineAngleCenter(
                    tile.drawx() + Mathf.sin(time, 6f, Vars.tilesize / 3f * size),
                    tile.drawy(),
                    90,
                    size * Vars.tilesize / 2f);

            Draw.reset();
        }
    }

    @Override
    public TileEntity newEntity() {
        return new HiveEntity();
    }

    public class HiveEntity extends CoreEntity {
        public float biomassTimer;
        public float biomassGoal = 60f * 60f * 3.5f;
    }
}

