package arc.entities;

import arc.Core;
import arc.entities.trait.DrawTrait;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Camera;
import arc.math.geom.Rect;

public class EntityDraw{
    private static final Rect viewport = new Rect();
    private static final Rect rect = new Rect();
    private static boolean clip = true;

    public static void setClip(boolean clip){ EntityDraw.clip = clip; }

    public static void draw(){ draw(Entities.defaultGroup()); }

    public static void draw(EntityGroup<?> group){ draw(group, e -> true); }

    public static <T extends DrawTrait> void draw(EntityGroup<?> group, Boolf<T> toDraw){
        drawWith(group, toDraw, DrawTrait::draw);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DrawTrait> void drawWith(EntityGroup<?> group, Boolf<T> toDraw, Cons<T> cons){
        if(clip){
            Camera cam = Core.camera;
            viewport.set(cam.position.x - cam.width / 2, cam.position.y - cam.height / 2, cam.width, cam.height);
        }

        group.forEach(e -> {
            if(!(e instanceof DrawTrait)) return;
            T t = (T) e;
            if(!toDraw.get(t) || !e.isAdded()) return;
            if(!clip || rect.setSize(((DrawTrait)e).drawSize()).setCenter(e.getX(), e.getY()).overlaps(viewport)){
                cons.get(t);
            }
        });
    }
}