package arc.entities;

import arc.entities.trait.Entity;
import arc.func.Cons;
import arc.func.Boolf;
import arc.math.geom.Rect;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.QuadTree;

public class EntityGroup<T extends Entity>{
    private static int lastid;
    private final boolean useTree;
    private final int id;
    private final Class<T> type;
    private final Seq<T> entityArray = new Seq<>(false, 16);
    private final Seq<T> entitiesToRemove = new Seq<>(false, 16);
    private final Seq<T> entitiesToAdd = new Seq<>(false, 16);
    private IntMap<T> map;
    private QuadTree<T> tree;
    private Cons<T> removeListener;
    private Cons<T> addListener;

    public EntityGroup(Class<T> type, boolean useTree){
        this.useTree = useTree;
        this.id = lastid++;
        this.type = type;
    }

    public boolean useTree(){ return useTree; }

    public void setRemoveListener(Cons<T> removeListener){ this.removeListener = removeListener; }
    public void setAddListener(Cons<T> addListener){ this.addListener = addListener; }

    public EntityGroup<T> enableMapping(){
        map = new IntMap<>();
        return this;
    }

    public boolean mappingEnabled(){ return map != null; }
    public Class<T> getType(){ return type; }
    public int getID(){ return id; }

    public void updateEvents(){
        for(T e : entitiesToAdd){
            if(e == null) continue;
            entityArray.add(e);
            e.added();
            if(map != null) map.put(e.getID(), e);
        }
        entitiesToAdd.clear();

        for(T e : entitiesToRemove){
            entityArray.remove(e, true);
            if(map != null) map.remove(e.getID());
            e.removed();
        }
        entitiesToRemove.clear();
    }

    public T getByID(int id){
        if(map == null) throw new RuntimeException("Mapping is not enabled for group " + id + "!");
        return map.get(id);
    }

    public void removeByID(int id){
        if(map == null) throw new RuntimeException("Mapping is not enabled for group " + id + "!");
        T t = map.get(id);
        if(t != null){
            remove(t);
        }else{
            for(T check : entitiesToAdd){
                if(check.getID() == id){
                    entitiesToAdd.remove(check, true);
                    if(removeListener != null) removeListener.get(check);
                    break;
                }
            }
        }
    }

    public QuadTree tree(){ return tree; }

    public void setTree(float x, float y, float w, float h){
        tree = new QuadTree<>(Entities.maxLeafObjects, new Rect(x, y, w, h));
    }

    public boolean isEmpty(){ return entityArray.size == 0; }
    public int size(){ return entityArray.size; }

    public int count(Boolf<T> pred){
        int count = 0;
        for(int i = 0; i < entityArray.size; i++){
            if(pred.get(entityArray.get(i))) count++;
        }
        return count;
    }

    public void add(T type){
        if(type == null) throw new RuntimeException("Cannot add a null entity!");
        if(type.getGroup() != null) return;
        type.setGroup(this);
        entitiesToAdd.add(type);
        if(mappingEnabled()) map.put(type.getID(), type);
        if(addListener != null) addListener.get(type);
    }

    public void remove(T type){
        if(type == null) throw new RuntimeException("Cannot remove a null entity!");
        type.setGroup(null);
        entitiesToRemove.add(type);
        if(removeListener != null) removeListener.get(type);
    }

    public void clear(){
        for(T entity : entityArray) entity.setGroup(null);
        for(T entity : entitiesToAdd) entity.setGroup(null);
        for(T entity : entitiesToRemove) entity.setGroup(null);
        entitiesToAdd.clear();
        entitiesToRemove.clear();
        entityArray.clear();
        if(map != null) map.clear();
    }

    public T find(Boolf<T> pred){
        for(int i = 0; i < entityArray.size; i++){
            if(pred.get(entityArray.get(i))) return entityArray.get(i);
        }
        return null;
    }

    public Seq<T> all(){ return entityArray; }

    public void forEach(Cons<T> cons){
        for(T t : entityArray){
            cons.get(t);
        }
    }
}