package arc.math.geom;

import arc.graphics.Camera;

public class OrthographicCamera extends Camera{
    public float zoom = 1f;

    public OrthographicCamera(){
    }

    public OrthographicCamera(float width, float height){
        setSize(width, height);
    }

    public void setSize(float width, float height){
        this.width = width;
        this.height = height;
    }
}
