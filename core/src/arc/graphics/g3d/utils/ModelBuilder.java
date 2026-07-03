package arc.graphics.g3d.utils;

import arc.graphics.g3d.Model;

public class ModelBuilder{
    public Model createBox(float w, float h, float d, Object material, long attributes){
        return new Model();
    }
    public void begin(){
    }
    public void part(Object mesh, int primitive, long attributes, Object material){
    }
    public Model end(){
        return new Model();
    }
}
