package io.anuke.mindustry.maps.filters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import io.anuke.mindustry.world.Tile;

import static io.anuke.mindustry.maps.filters.FilterOption.*;

public class MirrorFilter extends GenerateFilter{
    private static final Vector2 v1 = new Vector2(), v2 = new Vector2(), v3 = new Vector2();

    public int angle = 45;
    public boolean rotate = false;

    @Override
    public String name(){
        return "Mirror";
    }

    @Override
    public FilterOption[] options(){
        return new FilterOption[]{
            new SliderOption("angle", () -> (float)angle, f -> angle = (int)(float)f, 0f, 360f, 15f),
            new ToggleOption("rotate", () -> rotate, f -> rotate = f)
        };
    }

    @Override
    public void apply(GenerateInput in){
        v1.set(0, 1).setLength(1).setAngle(angle - 90);
        v2.set(v1).scl(-1f);

        v1.add(coord(0.5f, in.width), coord(0.5f, in.height));
        v2.add(coord(0.5f, in.width), coord(0.5f, in.height));

        v3.set(in.x, in.y);

        if(!left(v1, v2, v3)){
            mirror(in.width, in.height, v3, v1.x, v1.y, v2.x, v2.y);
            Tile tile = in.tile(v3.x, v3.y);
            if(tile != null){
                in.floor = tile.floor();
                if(!tile.block().synthetic()){
                    in.block = tile.block();
                }
            }
        }
    }

    float coord(float axis, int size){
        if(size <= 1) return 0f;
        return Math.max(0f, Math.min(1f, axis)) * (size - 1f);
    }

    void mirror(int width, int height, Vector2 p, float x0, float y0, float x1, float y1){
        if((width != height && angle % 90 != 0) || rotate){
            p.x = width - p.x - 1;
            p.y = height - p.y - 1;
        }else{
            float dx = x1 - x0;
            float dy = y1 - y0;

            float a = (dx * dx - dy * dy) / (dx * dx + dy * dy);
            float b = 2 * dx * dy / (dx * dx + dy * dy);

            p.set((a * (p.x - x0) + b * (p.y - y0) + x0), (b * (p.x - x0) - a * (p.y - y0) + y0));
        }
    }

    boolean left(Vector2 a, Vector2 b, Vector2 c){
        return ((b.x - a.x) * (c.y - a.y) > (b.y - a.y) * (c.x - a.x));
    }

    @Override
    public void drawOverlay(Pixmap pixmap, int pixW, int pixH, int mapW, int mapH){
        float cx = pixW / 2f;
        float cy = pixH / 2f;
        float rad = (float)Math.toRadians(angle - 90);

        float dirX = (float)Math.cos(rad);
        float dirY = (float)Math.sin(rad);

        float tMax = Math.max(pixW, pixH) * 2f;

        float x0 = cx + dirX * tMax;
        float y0 = cy + dirY * tMax;
        float x1 = cx - dirX * tMax;
        float y1 = cy - dirY * tMax;

        x0 = Math.max(0, Math.min(pixW - 1, x0));
        y0 = Math.max(0, Math.min(pixH - 1, y0));
        x1 = Math.max(0, Math.min(pixW - 1, x1));
        y1 = Math.max(0, Math.min(pixH - 1, y1));

        pixmap.setColor(Color.WHITE);
        drawLine(pixmap, (int)x0, (int)y0, (int)x1, (int)y1);
    }

    void drawLine(Pixmap pixmap, int x0, int y0, int x1, int y1){
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while(true){
            if(x0 >= 0 && x0 < pixmap.getWidth() && y0 >= 0 && y0 < pixmap.getHeight()){
                pixmap.drawPixel(x0, y0);
            }
            if(x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if(e2 > -dy){ err -= dy; x0 += sx; }
            if(e2 < dx){ err += dx; y0 += sy; }
        }
    }
}
