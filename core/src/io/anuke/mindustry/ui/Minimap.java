package io.anuke.mindustry.ui;

import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import io.anuke.mindustry.graphics.Shaders;
import arc.Core;
import arc.Graphics;
import arc.graphics.g2d.Draw;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.layout.Table;

import static io.anuke.mindustry.Vars.*;

public class Minimap extends Table{

    public Minimap(){
        super("pane");

        margin(5);

        TextureRegion r = new TextureRegion();

        Element elem = new Element(){
            @Override
            public void draw(){
                if(renderer.minimap.getRegion() == null) return;

                Draw.crect(renderer.minimap.getRegion(), x, y, width, height);

                if(renderer.minimap.getTexture() != null){
                    renderer.minimap.drawEntities(x, y, width, height);
                }

                if(showFog){
                    renderer.fog.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

                    r.setRegion(renderer.minimap.getRegion());
                    float pad = renderer.fog.getPadding();

                    float px = r.getU() * world.width() + pad;
                    float py = r.getV() * world.height() + pad;
                    float px2 = r.getU2() * world.width() + pad;
                    float py2 = r.getV2() * world.height() + pad;

                    r.setTexture(renderer.fog.getTexture());
                    r.setU(px / (world.width() + pad*2f));
                    r.setV(1f - py / (world.height() + pad*2f));
                    r.setU2(px2 / (world.width() + pad*2f));
                    r.setV2(1f - py2 / (world.height() + pad*2f));

                    Gfx.shader(Shaders.fog);
                    Draw.crect(r, x, y, width, height);
                    Gfx.shader();

                    renderer.fog.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
                }
            }
        };

        addListener(new InputListener(){
            public boolean scrolled(InputEvent event, float x, float y, int amount){
                renderer.minimap.zoomBy(amount);
                return true;
            }
        });

        elem.update(() -> {

            Element e = Core.scene.hit(Gfx.mouseWorld().x, Gfx.mouseWorld().y, true);
            if(e != null && e.isDescendantOf(this)){
                Core.scene.setScrollFocus(this);
            }else if(Core.scene.getScrollFocus() == this){
                Core.scene.setScrollFocus(null);
            }
        });

        add(elem).size(140f, 140f);

        elem.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                if(x >= 0 && x <= elem.getWidth() && y >= 0 && y <= elem.getHeight()){
                    ui.mapfrag.toggle();
                }
            }
        });
    }
}

