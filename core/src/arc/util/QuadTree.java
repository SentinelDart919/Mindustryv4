package arc.util;

import arc.entities.EntityCollisions.BoundingBoxProvider;
import arc.func.Cons;
import arc.math.geom.Rect;
import arc.struct.Seq;

import java.util.Iterator;

public class QuadTree<T>{
    private static Rect tmp = new Rect();
    private int maxObjectsPerNode;
    private int level;
    private Rect bounds;
    private Seq<T> objects;
    private BoundingBoxProvider<T> provider;
    private boolean leaf;
    private QuadTree<T> bottomLeftChild, bottomRightChild, topLeftChild, topRightChild;

    public QuadTree(int maxObjectsPerNode, Rect bounds){
        this(maxObjectsPerNode, 0, bounds, (obj, out) -> {
            if(obj instanceof QuadTreeObject){
                ((QuadTreeObject) obj).getHitbox(out);
            }else{
                throw new IllegalArgumentException("The provided object does not implement QuadTreeObject!");
            }
        });
    }

    private QuadTree(int maxObjectsPerNode, int level, Rect bounds, BoundingBoxProvider provider){
        this.level = level;
        this.bounds = bounds;
        this.maxObjectsPerNode = maxObjectsPerNode;
        this.provider = provider;
        objects = new Seq<>();
        leaf = true;
    }

    public void setBoundingBoxProvider(BoundingBoxProvider<T> prov){ this.provider = prov; }

    private void split(){
        if(!leaf) return;
        float subW = bounds.width / 2;
        float subH = bounds.height / 2;
        leaf = false;
        bottomLeftChild = new QuadTree<>(maxObjectsPerNode, level + 1, new Rect(bounds.x, bounds.y, subW, subH), provider);
        bottomRightChild = new QuadTree<>(maxObjectsPerNode, level + 1, new Rect(bounds.x + subW, bounds.y, subW, subH), provider);
        topLeftChild = new QuadTree<>(maxObjectsPerNode, level + 1, new Rect(bounds.x, bounds.y + subH, subW, subH), provider);
        topRightChild = new QuadTree<>(maxObjectsPerNode, level + 1, new Rect(bounds.x + subW, bounds.y + subH, subW, subH), provider);

        for(Iterator<T> iterator = objects.iterator(); iterator.hasNext(); ){
            T obj = iterator.next();
            provider.getBoundingBox(obj, tmp);
            QuadTree<T> child = getFittingChild(tmp);
            if(child != null){ child.insert(obj); iterator.remove(); }
        }
    }

    private void unsplit(){
        if(leaf) return;
        leaf = true;
        objects.addAll(bottomLeftChild.objects);
        objects.addAll(bottomRightChild.objects);
        objects.addAll(topLeftChild.objects);
        objects.addAll(topRightChild.objects);
        bottomLeftChild = bottomRightChild = topLeftChild = topRightChild = null;
    }

    public void insert(T obj){
        provider.getBoundingBox(obj, tmp);
        if(!bounds.overlaps(tmp)) return;
        if(leaf && (objects.size + 1) > maxObjectsPerNode) split();
        if(leaf){
            objects.add(obj);
        }else{
            provider.getBoundingBox(obj, tmp);
            QuadTree<T> child = getFittingChild(tmp);
            if(child != null) child.insert(obj);
            else objects.add(obj);
        }
    }

    public void remove(T obj){
        if(leaf){
            objects.remove(obj, true);
        }else{
            provider.getBoundingBox(obj, tmp);
            QuadTree<T> child = getFittingChild(tmp);
            if(child != null) child.remove(obj);
            else objects.remove(obj, true);
            if(getTotalObjectCount() <= maxObjectsPerNode) unsplit();
        }
    }

    public void clear(){
        objects.clear();
        if(bottomLeftChild != null) bottomLeftChild.clear();
        if(bottomRightChild != null) bottomRightChild.clear();
        if(topLeftChild != null) topLeftChild.clear();
        if(topRightChild != null) topRightChild.clear();
    }

    private QuadTree<T> getFittingChild(Rect boundingBox){
        float verticalMidpoint = bounds.x + (bounds.width / 2);
        float horizontalMidpoint = bounds.y + (bounds.height / 2);
        boolean topQuadrant = boundingBox.y > horizontalMidpoint;
        boolean bottomQuadrant = boundingBox.y < horizontalMidpoint && (boundingBox.y + boundingBox.height) < horizontalMidpoint;

        if(boundingBox.x < verticalMidpoint && boundingBox.x + boundingBox.width < verticalMidpoint){
            if(topQuadrant) return topLeftChild;
            if(bottomQuadrant) return bottomLeftChild;
        }else if(boundingBox.x > verticalMidpoint){
            if(topQuadrant) return topRightChild;
            if(bottomQuadrant) return bottomRightChild;
        }
        return null;
    }

    public QuadTree<T> getNodeAt(float x, float y){
        if(!bounds.contains(x, y)) return null;
        if(leaf) return this;
        if(topLeftChild.bounds.contains(x, y)) return topLeftChild.getNodeAt(x, y);
        if(topRightChild.bounds.contains(x, y)) return topRightChild.getNodeAt(x, y);
        if(bottomLeftChild.bounds.contains(x, y)) return bottomLeftChild.getNodeAt(x, y);
        if(bottomRightChild.bounds.contains(x, y)) return bottomRightChild.getNodeAt(x, y);
        return null;
    }

    public void getIntersect(Cons<T> out, Rect toCheck){
        if(!leaf){
            if(topLeftChild.bounds.overlaps(toCheck)) topLeftChild.getIntersect(out, toCheck);
            if(topRightChild.bounds.overlaps(toCheck)) topRightChild.getIntersect(out, toCheck);
            if(bottomLeftChild.bounds.overlaps(toCheck)) bottomLeftChild.getIntersect(out, toCheck);
            if(bottomRightChild.bounds.overlaps(toCheck)) bottomRightChild.getIntersect(out, toCheck);
        }
        for(int i = 0; i < objects.size; i++){
            provider.getBoundingBox(objects.get(i), tmp);
            if(tmp.overlaps(toCheck)) out.get(objects.get(i));
        }
    }

    public void getIntersect(Seq<T> out, Rect toCheck){
        if(!leaf){
            if(topLeftChild.bounds.overlaps(toCheck)) topLeftChild.getIntersect(out, toCheck);
            if(topRightChild.bounds.overlaps(toCheck)) topRightChild.getIntersect(out, toCheck);
            if(bottomLeftChild.bounds.overlaps(toCheck)) bottomLeftChild.getIntersect(out, toCheck);
            if(bottomRightChild.bounds.overlaps(toCheck)) bottomRightChild.getIntersect(out, toCheck);
        }
        out.addAll(objects);
    }

    public boolean isLeaf(){ return leaf; }
    public QuadTree<T> getBottomLeftChild(){ return bottomLeftChild; }
    public QuadTree<T> getBottomRightChild(){ return bottomRightChild; }
    public QuadTree<T> getTopLeftChild(){ return topLeftChild; }
    public QuadTree<T> getTopRightChild(){ return topRightChild; }
    public Rect getBounds(){ return bounds; }
    public Seq<T> getObjects(){ return objects; }

    public int getTotalObjectCount(){
        int count = objects.size;
        if(!leaf){
            count += topLeftChild.getTotalObjectCount();
            count += topRightChild.getTotalObjectCount();
            count += bottomLeftChild.getTotalObjectCount();
            count += bottomRightChild.getTotalObjectCount();
        }
        return count;
    }

    public void getAllChildren(Seq<T> out){
        out.addAll(objects);
        if(!leaf){
            topLeftChild.getAllChildren(out);
            topRightChild.getAllChildren(out);
            bottomLeftChild.getAllChildren(out);
            bottomRightChild.getAllChildren(out);
        }
    }

    public interface QuadTreeObject{
        void getHitbox(Rect out);
    }
}