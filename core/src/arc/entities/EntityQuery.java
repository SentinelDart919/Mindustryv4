package arc.entities;

import arc.entities.trait.Entity;
import arc.entities.trait.SolidTrait;
import arc.func.Boolf;
import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Seq;

public class EntityQuery{
    private static final EntityCollisions collisions = new EntityCollisions();
    private static final Seq<SolidTrait> array = new Seq<>();
    private static final Rect r1 = new Rect();

    public static EntityCollisions collisions(){ return collisions; }

    public static void init(float x, float y, float w, float h){
        for(EntityGroup group : Entities.getAllGroups()){
            if(group.useTree()){
                group.setTree(x, y, w, h);
            }
        }
    }

    public static void init(){ init(0, 0, 0, 0); }

    public static void resizeTree(float x, float y, float w, float h){ init(x, y, w, h); }

    public static void getNearby(EntityGroup<?> group, Rect rect, Cons<SolidTrait> out){
        if(!group.useTree())
            throw new RuntimeException("This group does not support quadtrees! Enable quadtrees when creating it.");
        group.tree().getIntersect(out, rect);
    }

    public static Seq<SolidTrait> getNearby(EntityGroup<?> group, Rect rect){
        array.clear();
        if(!group.useTree())
            throw new RuntimeException("This group does not support quadtrees! Enable quadtrees when creating it.");
        group.tree().getIntersect(array, rect);
        return array;
    }

    public static void getNearby(float x, float y, float size, Cons<SolidTrait> out){
        getNearby(Entities.defaultGroup(), r1.setSize(size).setCenter(x, y), out);
    }

    public static void getNearby(EntityGroup<?> group, float x, float y, float size, Cons<SolidTrait> out){
        getNearby(group, r1.setSize(size).setCenter(x, y), out);
    }

    public static Seq<SolidTrait> getNearby(float x, float y, float size){
        return getNearby(Entities.defaultGroup(), r1.setSize(size).setCenter(x, y));
    }

    public static Seq<SolidTrait> getNearby(EntityGroup<?> group, float x, float y, float size){
        return getNearby(group, r1.setSize(size).setCenter(x, y));
    }

    public static <T extends Entity> T getClosest(EntityGroup<T> group, float x, float y, float range, Boolf<T> pred){
        T closest = null;
        float cdist = 0f;
        Seq<SolidTrait> entities = getNearby(group, x, y, range * 2f);
        for(int i = 0; i < entities.size; i++){
            T e = (T) entities.get(i);
            if(!pred.get(e)) continue;
            float dist = Mathf.dst(e.getX(), e.getY(), x, y);
            if(dist < range)
                if(closest == null || dist < cdist){
                    closest = e;
                    cdist = dist;
                }
        }
        return closest;
    }

    public static void collideGroups(EntityGroup<?> groupa, EntityGroup<?> groupb){
        collisions().collideGroups(groupa, groupb);
    }
}