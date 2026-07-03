package io.anuke.mindustry.world.consumers;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.meta.BlockStats;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.mobile;

public abstract class Consume{
    protected boolean optional;
    protected boolean update = true;

    public Consume optional(boolean optional){
        this.optional = optional;
        return this;
    }

    public Consume update(boolean update){
        this.update = update;
        return this;
    }

    public boolean isOptional(){
        return optional;
    }

    public boolean isUpdate(){
        return update;
    }

    public void build(Table table){
        Table t = new Table();
        t.margin(4);
        buildTooltip(t);

        int scale = mobile ? 4 : 3;

        table.table((Cons<Table>)out -> {
            out.image(Core.atlas.find(getIcon())).size(10 * scale).color(Color.darkGray).padRight(-10 * scale).padBottom(-scale * 2);
            out.image(Core.atlas.find(getIcon())).size(10 * scale).color(Palette.accent);
            out.image(Core.atlas.find("icon-missing")).size(10 * scale).color(Palette.remove).padLeft(-10 * scale);
        }).size(10 * scale).get().addListener(new Tooltip((Cons<Table>)tooltip -> tooltip.add(t)));
    }

    public abstract void buildTooltip(Table table);

    public abstract String getIcon();

    public abstract void update(Block block, TileEntity entity);

    public abstract boolean valid(Block block, TileEntity entity);

    public abstract void display(BlockStats stats);
}
