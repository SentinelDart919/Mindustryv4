package io.anuke.mindustry.ui.fragments;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Gfx;
import arc.math.geom.Vec2;
import arc.util.Align;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import io.anuke.mindustry.core.GameState.State;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.ui.ItemImage;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.units.UnitFactoryAdvanced;
import io.anuke.mindustry.world.consumers.Consume;
import arc.Graphics;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.*;

public class BlockConsumeFragment extends Fragment{
    private Table table;
    private Tile lastTile;
    private boolean visible;

    @Override
    public void build(Group parent){
        table = new Table();
        table.visible(() -> !state.is(State.menu) && visible);
        table.setTransform(true);

        parent.addChild(new Element(){{update(() -> {
            Tile tile = world.tileWorld(Gfx.mouseWorld().x, Gfx.mouseWorld().y);
            if(tile == null) return;
            tile = tile.target();

            if(tile != lastTile){
                if(tile.getTeam() == players[0].getTeam() && tile.block().consumes.hasAny()){
                    show(tile);
                }else if(visible){
                    hide();
                }
                lastTile = tile;
            }
        });}});

        parent.setTransform(true);
        parent.addChild(table);
    }

    public void show(Tile tile){
        ObjectSet<Consume> consumers = new ObjectSet<>();
        TileEntity entity = tile.entity;
        Block block = tile.block();

        table.clearChildren();

        rebuild(block, entity);
        visible = true;

        table.update(() -> {

            if(tile.entity == null || state.is(State.menu)){
                hide();
                return;
            }

            boolean rebuild = false;

            for(Consume c : block.consumes.array()){
                boolean valid = c.isOptional() || c.valid(block, entity);

                if(consumers.contains(c) == valid){
                    if(valid){
                        consumers.remove(c);
                    }else{
                        consumers.add(c);
                    }
                    rebuild = true;
                }
            }

            if(rebuild){
                rebuild(block, entity);
            }

            Vec2 v = Core.camera.project(new Vec2(tile.drawx() - tile.block().size * tilesize / 2f + 0.25f, tile.drawy() + tile.block().size * tilesize / 2f));
            table.pack();
            table.setPosition(v.x, v.y, Align.topRight);
        });

        table.act(Core.graphics.getDeltaTime());
    }

    public void hide(){
        table.clear();
        table.update(() -> {});
        visible = false;
    }

    private void rebuild(Block block, TileEntity entity){
        table.clearChildren();
        table.left();

        int scale = mobile ? 4 : 3;

        for(Consume c : block.consumes.array()){
            if(!c.isOptional() && !c.valid(block, entity)){
                boolean[] hovered = {false};

                table.table("inventory", c::buildTooltip).visible(() -> hovered[0]).height(scale * 10 + 6).padBottom(-4).right().update(t -> {
                    if(t.getChildren().size == 0) t.remove();
                }).get().act(0);

                Table result = table.table(out -> {
                    out.image(c.getIcon()).size(10 * scale).color(Color.darkGray).padRight(-10 * scale).padBottom(-scale * 2);
                    out.image(c.getIcon()).size(10 * scale).color(Palette.accent);
                    out.image("icon-missing").size(10 * scale).color(Palette.remove).padLeft(-10 * scale);
                }).size(10 * scale).get();

                result.hovered(() -> hovered[0] = true);
                if(!mobile){
                    result.exited(() -> hovered[0] = false);
                }

                table.row();
            }
        }

        if(block instanceof UnitFactoryAdvanced){
            UnitFactoryAdvanced factory = (UnitFactoryAdvanced)block;

            if(!factory.hasAllRequirements(entity.tile)){
                table.table("inventory", t -> {
                    for(ItemStack stack : factory.getRequirements(entity.tile)){
                        t.add(new ItemImage(stack)).size(8 * 4).padRight(5);
                    }
                }).height(scale * 10 + 6).padBottom(-4).right();
                table.row();
            }
        }
    }
}


