package io.anuke.mindustry.ui.fragments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.anuke.mindustry.graphics.Shaders;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Lines;
import io.anuke.ucore.scene.Element;
import io.anuke.ucore.scene.Group;
import io.anuke.ucore.scene.event.InputEvent;
import io.anuke.ucore.scene.event.InputListener;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.*;

public class MapFragment extends Fragment{
    private boolean visible = false;
    private float zoom = 1f;
    private Vector2 offset = new Vector2();
    private Vector2 lastMouse = new Vector2();

    @Override
    public void build(Group parent){
        parent.fill(table -> {
            table.visible(() -> visible);

            TextureRegion r = new TextureRegion();

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

                    Draw.color(Color.BLACK);
                    Draw.rect("blank", this.x + width / 2f, this.y + height / 2f, width, height);
                    Draw.color();

                    Graphics.beginClip(this.x, this.y, width, height);

                    r.setRegion(renderer.minimap.getTexture());
                    Draw.crect(r, x, y, displayWidth, displayHeight);

                    float worldWidth = world.width() * tilesize;
                    float worldHeight = world.height() * tilesize;

                    Units.getAllUnits(unit -> {
                        Tile tile = world.tileWorld(unit.x, unit.y);
                        if(showFog && players.length > 0 && unit.getTeam() != players[0].getTeam() && (tile == null || tile.getVisibility() == 0)) return;

                        float rx = (unit.x / worldWidth) * displayWidth;
                        float ry = (unit.y / worldHeight) * displayHeight;
                        Draw.color(unit.getTeam().color);
                        Draw.rect("blank", x + rx, y + ry, Math.max(zoom * 2f, 4f), Math.max(zoom * 2f, 4f));
                    });

                    if(showFog){
                        renderer.fog.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

                        float pad = renderer.fog.getPadding();
                        float fw = world.width() + pad * 2f;
                        float fh = world.height() + pad * 2f;

                        r.setTexture(renderer.fog.getTexture());
                        r.setU(pad / fw);
                        r.setV(1f - pad / fh);
                        r.setU2((world.width() + pad) / fw);
                        r.setV2(1f - (world.height() + pad) / fh);

                        Graphics.shader(Shaders.fog);
                        Draw.crect(r, x, y, displayWidth, displayHeight);
                        Graphics.shader();

                        renderer.fog.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
                    }

                    Draw.color(Color.WHITE);
                    float camX = (Core.camera.position.x / worldWidth) * displayWidth;
                    float camY = (Core.camera.position.y / worldHeight) * displayHeight;
                    float camW = (Core.camera.viewportWidth / worldWidth) * displayWidth;
                    float camH = (Core.camera.viewportHeight / worldHeight) * displayHeight;

                    Lines.stroke(2f);
                    Lines.rect(x + camX - camW / 2f, y + camY - camH / 2f, camW, camH);

                    Graphics.endClip();
                }
            };

            elem.addListener(new InputListener(){
                private float lastDistance = 0;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                    if(pointer == 0) lastMouse.set(x, y);
                    if(pointer == 1) lastDistance = 0;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer){
                    if(Gdx.input.isTouched(0) && Gdx.input.isTouched(1)){
                        float dist = Vector2.dst(Gdx.input.getX(0), Gdx.input.getY(0), Gdx.input.getX(1), Gdx.input.getY(1));
                        if(lastDistance != 0){
                            zoom = Mathf.clamp(zoom + (dist - lastDistance) * 0.005f * zoom, 0.5f, 20f);
                        }
                        lastDistance = dist;
                    }else if(pointer == 0 && !Gdx.input.isTouched(1)){
                        offset.add((x - lastMouse.x) / zoom, (y - lastMouse.y) / zoom);
                    }

                    if(pointer == 0) lastMouse.set(x, y);
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                    if(pointer == 1) lastDistance = 0;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, int amount){
                    float oldZoom = zoom;
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
