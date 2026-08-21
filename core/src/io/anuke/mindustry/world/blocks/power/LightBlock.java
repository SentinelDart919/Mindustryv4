package io.anuke.mindustry.world.blocks.power;

import com.badlogic.gdx.graphics.Color;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.graphics.Shaders;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.PowerBlock;
import io.anuke.mindustry.ui.dialogs.ColorPickDialog;
import io.anuke.ucore.graphics.Hue;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Tmp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LightBlock extends PowerBlock {
    public float brightness = 0.9f;
    public float radius = 80f;

    public LightBlock(String name) {
        super(name);
        hasPower = true;
        update = true;
        configurable = true;
    }

    @Override
    public void init() {
        super.init();
        emitLight = true;
    }

    @Override
    public float lightRadius() {
        return radius;
    }

    @Override
    public void drawLight(Tile tile){
        LightEntity entity = tile.entity();
        float radius = entity.radius < 0f ? this.radius : entity.radius;
        float brightness = entity.brightness < 0f ? this.brightness : entity.brightness;
        float opacity = brightness * (entity.power.graph == null ? 0f : 1f);

        if(radius > 0.001f && opacity > 0.001f){
            Draw.color(Tmp.c1.set(entity.color));
            Shaders.light.region = Draw.region("circle");
            Draw.alpha(opacity);
            Draw.rect("circle", tile.drawx(), tile.drawy(), radius * 2, radius * 2);
            Draw.alpha(opacity * 0.5f);
            Draw.rect("circle", tile.drawx(), tile.drawy(), radius * 2, radius * 2);
        }
    }

    @Override
    public void buildTable(Tile tile, Table table) {
        LightEntity entity = tile.entity();

        table.addImageButton("icon-pencil", 40, () -> {
            new ColorPickDialog().show(color -> entity.color = Hue.rgb(color));
        });
    }

    @Override
    public TileEntity newEntity() {
        return new LightEntity();
    }

    public class LightEntity extends TileEntity {
        public int color = Hue.rgb(Color.WHITE);
        public float radius = -1f;
        public float brightness = -1f;

        @Override
        public void write(DataOutput stream) throws IOException {
            super.write(stream);
            stream.writeInt(color);
        }

        @Override
        public void read(DataInput stream) throws IOException {
            super.read(stream);
            color = stream.readInt();
        }

        @Override
        public void writeConfig(DataOutput stream) throws IOException {
            stream.writeInt(color);
        }

        @Override
        public void readConfig(DataInput stream) throws IOException {
            color = stream.readInt();
        }

        @Override
        public Object config() {
            return color;
        }

        @Override
        public void configured(Object config) {
            if (config instanceof Integer) {
                color = (Integer) config;
            }
        }
    }
}
