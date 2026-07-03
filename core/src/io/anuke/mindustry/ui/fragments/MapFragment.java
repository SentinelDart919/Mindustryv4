package io.anuke.mindustry.ui.fragments;

import arc.Core;
import arc.graphics.Gfx;
import arc.graphics.Color;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import io.anuke.mindustry.graphics.Shaders;
import io.anuke.mindustry.entities.Units;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.input.KeyCode;
import arc.math.Mathf;

import static io.anuke.mindustry.Vars.*;

public class MapFragment extends Fragment{
    private boolean visible = false;
    private float zoom = 1f;
    private Vec2 offset = new Vec2();
    private Vec2 lastMouse = new Vec2();

    @Override
    public void build(Group parent){
        parent.fill(table -> {
            table.visible(() -> visible);

            Element elem = new Element(){
                @Override
                public void draw(){
                    if(renderer.minimap.getTexture() == null) return;

                    float size = Math.min(width, height);
                    float mapWidth = world.width();
                    float mapHeight = world.height();
                    float maxMapSize = Math.max(mapWidth, mapHeight);

                    float displayWidth = mapWidth / maxMapSize * size * zoom;
                    float displayHeight = mapHeight / maxMapSize * size * zoom;

                    float x = this.x + width / 2f + offset.x * zoom - displayWidth / 2f;
                    float y = this.y + height / 2f + offset.y * zoom - displayHeight / 2f;

                    Draw.color(Color.black);
                    Draw.rect("blank", this.x + width / 2f, this.y + height / 2f, width, height);
                    Draw.color();

                    Gfx.beginClip(this.x, this.y, width, height);

                    Draw.rect(new TextureRegion(renderer.minimap.getTexture()), x, y, displayWidth, displayHeight);

                    float worldWidth = world.width() * tilesize;
                    float worldHeight = world.height() * tilesize;

                    Units.getAllUnits(unit -> {
                        if(showFog && players.length > 0 && unit.getTeam() != players[0].getTeam() && world.tileWorld(unit.x, unit.y).getVisibility() == 0) return;

                        float rx = (unit.x / worldWidth) * displayWidth;
                        float ry = (unit.y / worldHeight) * displayHeight;
                        Draw.color(unit.getTeam().color);
                        Draw.rect("blank", x + rx, y + ry, Math.max(zoom * 2f, 4f), Math.max(zoom * 2f, 4f));
                    });

                    if(showFog){
                        renderer.fog.getTexture().setFilter(TextureFilter.nearest, TextureFilter.nearest);

                        float pad = renderer.fog.getPadding();
                        float fw = world.width() + pad * 2f;
                        float fh = world.height() + pad * 2f;

                        TextureRegion fog = new TextureRegion(renderer.fog.getTexture());
                        fog.setU(pad / fw);
                        fog.setV(1f - pad / fh);
                        fog.setU2((world.width() + pad) / fw);
                        fog.setV2(1f - (world.height() + pad) / fh);

                        Gfx.shader(Shaders.fog);
                        Draw.rect(fog, x, y, displayWidth, displayHeight);
                        Gfx.shader();

                        renderer.fog.getTexture().setFilter(TextureFilter.linear, TextureFilter.linear);
                    }

                    Draw.color(Color.white);
                    float camX = (Core.camera.position.x / worldWidth) * displayWidth;
                    float camY = (Core.camera.position.y / worldHeight) * displayHeight;
                    float camW = (Core.camera.width / worldWidth) * displayWidth;
                    float camH = (Core.camera.height / worldHeight) * displayHeight;

                    Lines.stroke(2f);
                    Lines.rect(x + camX - camW / 2f, y + camY - camH / 2f, camW, camH);

                    Gfx.endClip();
                }
            };

            elem.addListener(new InputListener(){
                private float lastDistance = 0;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(pointer == 0) lastMouse.set(x, y);
                    if(pointer == 1) lastDistance = 0;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer){
                    if(Core.input.isTouched(0) && Core.input.isTouched(1)){
                        float dist = Mathf.dst(Core.input.mouseX(0), Core.input.mouseY(0), Core.input.mouseX(1), Core.input.mouseY(1));
                        if(lastDistance != 0){
                            zoom = Mathf.clamp(zoom + (dist - lastDistance) * 0.005f * zoom, 0.5f, 20f);
                        }
                        lastDistance = dist;
                    }else if(pointer == 0 && !Core.input.isTouched(1)){
                        offset.add((x - lastMouse.x) / zoom, (y - lastMouse.y) / zoom);
                    }

                    if(pointer == 0) lastMouse.set(x, y);
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(pointer == 1) lastDistance = 0;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                    float oldZoom = zoom;
                    float amount = amountY;
                    zoom = Mathf.clamp(zoom - amount * 0.2f * zoom, 0.5f, 20f);

                    float worldX = (x - (elem.getWidth() / 2f + offset.x * oldZoom)) / oldZoom;
                    float worldY = (y - (elem.getHeight() / 2f + offset.y * oldZoom)) / oldZoom;

                    offset.x = (x - elem.getWidth() / 2f - worldX * zoom) / zoom;
                    offset.y = (y - elem.getHeight() / 2f - worldY * zoom) / zoom;
                    return true;
                }
            });

            table.add(elem).grow();
        });

        parent.fill(table -> {
            table.visible(() -> visible);
            table.top().left();
            table.addImageButton("icon-cancel", "clear", 40, this::toggle).size(64).pad(10);
        });
    }

    public void toggle(){
        visible = !visible;
        if(visible){
            zoom = 1f;
            offset.set(0, 0);
        }
    }

    public boolean isOpen(){
        return visible;
    }
}

